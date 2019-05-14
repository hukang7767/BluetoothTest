package com.huaxindata.bluetoothtest.util;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by sunanoe on 2017/7/5.
 * 自定义的一个Log类，用于记录检蓝牙测过程
 * 将log保存7天，之后自动删除7天之前的日志
 */

public class HxLog {
    public final static boolean isDebug=false;
    //日志文件根目录
    private static String LOG_DIR;
    private static LogThread mThread;

    public static void e(String tag, String msg){
        if (isDebug) {
            return ;
        }
        mThread.println(tag,msg);
    }

    /**
     * 在activity中注册log，初始化Log需要的资源
     */
    public static void registerLog(Context context){
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            final File dataDirectory = Environment.getExternalStorageDirectory();
            LOG_DIR = dataDirectory.getAbsolutePath() + File.separator + "BluetoothTest";
        } else {
            final File filesDir = context.getFilesDir();
            LOG_DIR=filesDir.getAbsolutePath()+File.separator+"BluetoothTest";
        }
        mThread = new LogThread();
        mThread.start();
    }
    /**
     * 释放log占用的资源
     */
    public static void unRegisterLog(){
        mThread.cancel();
        mThread=null;
    }

    private static class LogThread extends Thread{
        private static File logFile;
        private static FileWriter mFileWriter;
        private static BufferedWriter mWriter;
        @Override
        public void run() {
            final long currentTimeMillis = System.currentTimeMillis();
            String date = getDateString(currentTimeMillis);//不取时分秒
            //删除过期的日志，只保留7天
            File dir = new File(LOG_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            final File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    String fileName = file.getName();
                    //2017年，截取文件名后面的日期，注意要加上时分秒
                    final String fileDate = fileName.substring(fileName.indexOf('2'), fileName.lastIndexOf('.'));
                    if (fileDate.equals(date)) {//今天的文件是否存在
                        logFile = file;
                        continue;
                    }
                    Long time = getDateLong(fileDate + " 00:00:00");
                    if (currentTimeMillis - time > 7 * 24 * 60 * 60*1000) {
                        file.delete();
                    }
                }
            }
            if (logFile == null || !logFile.exists()) {
                logFile = new File(LOG_DIR, "log_"+date+".txt");
            }
            try
            {
                mFileWriter = new FileWriter(logFile,true);
                mWriter = new BufferedWriter(mFileWriter);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        private static boolean flag;//将mWriter为空这段时间的日志写入到集合中保存着
        private static List<String> tagList = new ArrayList<>(30);
        private static List<String> logList = new ArrayList<>(30);
        public static void println(String tag,String string){
            android.util.Log.e(tag,string);
            final long timeMillis = System.currentTimeMillis();
            if (mWriter==null) {
                flag = true;
                tagList.add(getTimeString(timeMillis)+'\t'+tag);
                logList.add(string);
                return;
            }
            if (flag) {
                final int size = tagList.size();
                for (int i=0;i<size;i++) {
                    try {
                        mWriter.write(tagList.get(i)+'\t');
                        mWriter.write(string+'\n');
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            flag=false;
            try {
                mWriter.write(getTimeString(timeMillis)+'\t');//写入时间
                mWriter.write(tag+'\t');
                mWriter.write(string+'\n');
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public static void cancel(){
            try {
                if (mFileWriter != null||mWriter != null) {
                    mFileWriter.close();
                    mWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    mFileWriter.close();
                    mWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        private static String getTimeString(long time) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd HH:mm:ss");
            String date = format.format(time);
            return date;
        }
        private static String getDateString(long time) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd");
            String date = format.format(time);
            return date;
        }
        private static long getDateLong(String time){
            SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd HH:mm:ss");
            Date date=null;
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
}
