package com.huaxindata.bluetoothtest.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;

import com.huaxindata.bluetoothtest.activity.SettingActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * log日志统计保存
 *
 * @author way
 */

@SuppressLint("SimpleDateFormat")
public class LogcatHelper {

    private static LogcatHelper INSTANCE = null;
    private static String PATH_LOGCAT;
    private LogDumper mLogDumper = null;
    private int mPId;
    public static boolean isDebug = true;

    /**
     * 初始化目录
     */
    public void init(Context context) {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {// 优先保存到SD卡中
            PATH_LOGCAT = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + File.separator + "HxLog";
        } else {// 如果SD卡不存在，就保存到本应用的目录下
            PATH_LOGCAT = context.getFilesDir().getAbsolutePath()
                    + File.separator + "HxLog";
        }
        File file = new File(PATH_LOGCAT);
        if (!file.exists()) {
            file.mkdirs();
        }
        isDebug = Configuration.getLogOpen(context);
    }

    public static LogcatHelper getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new LogcatHelper(context);
        }
        return INSTANCE;
    }

    private LogcatHelper(Context context) {
        init(context);
        mPId = android.os.Process.myPid();
    }

    public void start() {
        if (mLogDumper == null)
            mLogDumper = new LogDumper(String.valueOf(mPId), PATH_LOGCAT);
        mLogDumper.start();
    }

    public void stop() {
        if (mLogDumper != null) {
            mLogDumper.stopLogs();
            mLogDumper = null;
        }
    }

    private class LogDumper extends Thread {

        private Process logcatProc;
        private BufferedReader mReader = null;
        private boolean mRunning = true;
        String cmds = null;
        private String mPID;
        private FileOutputStream out = null;

        public LogDumper(String pid, String dir) {
            mPID = pid;
            try {
                out = new FileOutputStream(new File(dir, "Hx-" + getFileName()
                        + ".txt"), true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            /**
             *
             * 日志等级：*:v , *:d , *:w , *:e , *:f , *:s
             *
             * 显示当前mPID程序的 E和W等级的日志.
             *
             * */

            // cmds = "logcat *:e *:w | grep \"(" + mPID + ")\"";
            // cmds = "logcat  | grep \"(" + mPID + ")\"";//打印所有日志信息
            // cmds = "logcat -s way";//打印标签过滤信息
            if (String.valueOf(mPId).length() < 4) {
                cmds = "logcat *:e *:e | grep \"(0" + mPID + ")\"";
            } else {
                cmds = "logcat *:e *:e | grep \"(" + mPID + ")\"";
            }
            // logcat -v time -f /sdcard/locker.log *:W &
            System.out.println(dir + " cmds:" + cmds);

        }



        public void stopLogs() {
            mRunning = false;
        }

        @Override
        public void run() {
            final long currentTimeMillis = System.currentTimeMillis();
            File fileDir = new File(PATH_LOGCAT);
            final File[] files = fileDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    String fileName = file.getName();
                    //2017年，截取文件名后面的日期，注意要加上时分秒
                    final String fileDate = fileName.substring(fileName.indexOf('2'), fileName.lastIndexOf('.'));
                    Long time = getDateLong(fileDate + " 00:00:00");
                    if (currentTimeMillis - time > 7 * 24 * 60 * 60 * 1000) {
                        file.delete();
                    }
                }
            }
            if (isDebug) {
                try {
                    logcatProc = Runtime.getRuntime().exec(cmds);
                    mReader = new BufferedReader(new InputStreamReader(
                            logcatProc.getInputStream()), 1024);
                    String line = null;
                    while (mRunning && (line = mReader.readLine()) != null) {
                        if (!mRunning) {
                            break;
                        }
                        if (line.length() == 0) {
                            continue;
                        }
                        if (out != null && line.contains(mPID)) {
                            out.write((getDateEN() + "  " + line + "\n").getBytes());
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (logcatProc != null) {
                        logcatProc.destroy();
                        logcatProc = null;
                    }
                    if (mReader != null) {
                        try {
                            mReader.close();
                            mReader = null;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        out = null;
                    }

                }
            }


        }

    }

    public static String getFileName() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd");
        String date = format.format(new Date(System.currentTimeMillis()));
        return date;// 2012年10月03日 23:41:31
    }

    @SuppressLint("SimpleDateFormat")
    public static String getDateEN() {
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy_MM_dd HH:mm:ss");
        String date1 = format1.format(new Date(System.currentTimeMillis()));
        return date1;// 2012-10-03 23:41:31
    }

    private static long getDateLong(String time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd HH:mm:ss");
        Date date = null;
        try {
            date = format.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (date == null) {
            return 0;
        }
        return date.getTime();
    }
}

