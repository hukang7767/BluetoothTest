package com.huaxindata.bluetoothtest.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huaxin.emmp_ard.scancapture.ScanCaptureActivity;
import com.huaxindata.bluetoothtest.CarApp;
import com.huaxindata.bluetoothtest.ListenSoundListener;
import com.huaxindata.bluetoothtest.R;
import com.huaxindata.bluetoothtest.entity.NetConfig;
import com.huaxindata.bluetoothtest.entity.SpeechListenner;
import com.huaxindata.bluetoothtest.entity.VinBean;
import com.huaxindata.bluetoothtest.util.ClsUtils;
import com.huaxindata.bluetoothtest.util.Configuration;
import com.huaxindata.bluetoothtest.util.DBManager;
import com.huaxindata.bluetoothtest.util.LogcatHelper;
import com.huaxindata.bluetoothtest.util.StateInfo;
import com.huaxindata.bluetoothtest.util.TestInfoDTO;
import com.huaxindata.bluetoothtest.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 蓝牙检测主界面
 *
 * @author PHJ
 * @time 2014/11/17
 */
public class MainHomeActivtiy extends Activity implements OnClickListener, StateInfo {

    public static final String TAG = "MainHomeActivtiy";
    private static int count = 3;//准备检测的倒数321
    public TestThread mTestThread = null;
    public static MainHomeActivtiy mContext;
    //数据存储与读取-------------------------
    private static DBManager sDBManager;

    private static int openTimes = 0;
    private static Animation operatingAnim;// 按钮动画

    private FrameLayout mStartTestBtn;//中间的开始检测的整个布局
    private ImageView mProgressImg;//中间显示正在检测动画的imgView
    private TextView mTestStateHintTv;//中间显示是否正在检测中的TextView

    private LinearLayout mCancelLayout;//取消检测的布局
    private LinearLayout mSettingsLayout;//进入设置页面的布局

    private TextView mTestProcessHintTv;//提示蓝牙连接的textView
    private TextView mVinHintTv;//提示已经有vin接入的textView

    public static ConnectServerThread sConnectServerThread;

    private static int MAX_TEST = NetConfig.getMaxTest();

    private static boolean initListenerOk = false;
    public static boolean isTestOver = true;//是否检测完毕

    //结果是否成功发送给服务器
    private static boolean sIsSendResultSuccess = false;
    private static int listenIndex = 0;// 听的次数
    // 接受音频识别控件
    //	private static ListenSound listenSound;
    private static AudioManager mAudioManager;
    private static boolean sListenResult = false;
    private static long startTime;
    private SpeechListenner mSpeechListenner;//讯飞语音识别听音器
    private Timer mTimer = null;
    private Button mRestartBtn;
    private boolean isReStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mContext = this;
        sDBManager = new DBManager(this);
        // 初始化控件
        initListenSound();
        initView();
        // 初始化动画组件
        initAnim();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setMode(AudioManager.MODE_IN_CALL);
        LogcatHelper.getInstance(this).start();
        initThread();
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        isReStart = false;
    }

    private void initTimer() {
        mTimer = new Timer(true);
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (canStart()) {
                    startTest();
                }
            }
        }, 500, 3000);
    }

    private void initThread() {
        if (sConnectServerThread == null)
            sConnectServerThread = new ConnectServerThread(this);
        if (NetConfig.getIP() == null) {
            buildIpDialog();
        }
        sConnectServerThread.start();
    }

    private void buildIpDialog() {
        Builder builder = new Builder(
                MainHomeActivtiy.mContext);
        builder.setMessage("请先设置IP");
        builder.setTitle("提示");
        builder.setOnKeyListener(CarApp.keylistener).setCancelable(false);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(MainHomeActivtiy.this, SettingIPActivity.class);
                intent.putExtra("in", "home");
                startActivity(intent);
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void initAnim() {
        operatingAnim = AnimationUtils.loadAnimation(this, R.anim.tip);
        LinearInterpolator lin = new LinearInterpolator();
        operatingAnim.setInterpolator(lin);
    }

    private void initView() {
        mStartTestBtn = (FrameLayout) findViewById(R.id.home_button_centre);
        mTestStateHintTv = (TextView) findViewById(R.id.home_button_msg);
        mProgressImg = (ImageView) findViewById(R.id.home_button_progressbar);

        mSettingsLayout = (LinearLayout) findViewById(R.id.home_button_setting);
        mCancelLayout = (LinearLayout) findViewById(R.id.home_button_cancel);

        mTestProcessHintTv = (TextView) findViewById(R.id.home_msg_show);
        mVinHintTv = (TextView) findViewById(R.id.home_msg_show_vin);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        //设置监听器
        mCancelLayout.setOnClickListener(this);
        mSettingsLayout.setOnClickListener(this);
        mStartTestBtn.setOnClickListener(this);
        mRestartBtn.setOnClickListener(this);


        TextView tag = (TextView) findViewById(R.id.home_tag);
        tag.setBackgroundResource(R.drawable.tag_main);
        tag.setText(getString(R.string.showtag_main));
        String vinTextTV = mVinHintTv.getText().toString();
        if (!isReStart) {
            queryVin();
        }

    }

    private void initListenSound() {
        Log.e(TAG, "initListenSound: ========初始化检测语音");
        mSpeechListenner = new SpeechListenner(mContext, new ListenSoundListener() {
            // 听初始化成功后的处理
            @Override
            public void init(boolean isok) {
                listenIndex = 0;
                if (isok) {// 初始化成功
                    initListenerOk = true;
                    Log.e(TAG, "init: =================初始化检测声音ok");
                } else {
                    toast("初始化语音失败，退出测试");
                    Log.e(TAG, "init: ================初始化语音失败,退出测试");
                }
            }

            // 听完成后的处理
            @Override
            public void listenMsg(String msg, boolean isEnd) {
                Log.e(TAG, "listenMsg: ==============测试声音回调===Msg:" + msg);
                /** 开始检测第四步，处理听到的结果 */
                if (msg != null) {
                    Log.e(TAG, "listenMsg: ====================================声音检测ok");
                    sListenResult = true;
                    isTestOver = true;
                    mSpeechListenner.cancel();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //检查是否能开始检测
        //		mHandler.sendEmptyMessage(CHECK_START);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        boolean resetNet = intent.getBooleanExtra("resetNet", false);
        if (resetNet) {
            MAX_TEST = NetConfig.getMaxTest();
            Log.e("xxx", "onNewIntent:========重新设置了Ip地址");
            new Thread() {
                @Override
                public void run() {
                    sConnectServerThread.kill();
                    //					NetWorkService.restart();
                }
            }.start();
        }
    }

    //与子进程通信的handler
    private MainUiHandler mHandler = new MainUiHandler(this);

    private static class MainUiHandler extends Handler {
        MainHomeActivtiy mActivtiy;
        WeakReference<MainHomeActivtiy> mWeakReference;

        public MainUiHandler(MainHomeActivtiy activtiy) {
            mWeakReference = new WeakReference<>(activtiy);
            mActivtiy = mWeakReference.get();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RELEASE:
                    mActivtiy.reset();
                    break;
                //检查是否可以开始
                case CHECK_START:
                    synchronized (msg) {
                        if (mActivtiy.canStart()) {
                            mActivtiy.mHandler.removeMessages(CHECK_START);
                            if (mActivtiy.mTestThread == null) {
                                mActivtiy.startTest();
                            }
                        } else {//如果不能开始检测，则1秒钟之后再检查
                            mActivtiy.mHandler.sendEmptyMessageDelayed(CHECK_START, 2000);
                        }
                    }
                    break;
                case START_ANIMATION:
                    mActivtiy.startAnim(mActivtiy.mProgressImg);
                    break;
                case UPDATE_ANIMATION:
                    mActivtiy.mTestProcessHintTv.setText(R.string.test_reday);
                    mActivtiy.mTestStateHintTv.setText("" + count--);
                    if (count == 0) {
                        mActivtiy.mTestProcessHintTv.setText(R.string.test_listen_init);
                        mActivtiy.mTestStateHintTv.setText(R.string.test_in);
                    }
                    break;
                case LISTENER_OVER:
                    mActivtiy.mTestProcessHintTv.setText(R.string.test_listen_ok);
                    break;
                case SEND_RESULT:
                    mActivtiy.mTestProcessHintTv.setText(R.string.test_over_send);
                    break;
                case SEND_OK:
                    sIsSendResultSuccess = true;
                    mActivtiy.mTestProcessHintTv.setText("发送检测结果成功");
                    try {
                        final Runtime runtime = Runtime.getRuntime();
                        final Process process = runtime.exec("pm clear com.android.bluetooth");
                        runtime.exec("rm /data/misc/bluedroid/*.*");
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        mActivtiy.mTestProcessHintTv.setText("发送检测结果成功");
                        if (mActivtiy.mTimer!=null){
                            mActivtiy.mTimer.cancel();
                            mActivtiy.mTimer = null;
                        }
                    }
                    break;
                case SEND_NG:
                    mActivtiy.mTestProcessHintTv.setText("发送检测结果失败");
                    break;
                case TEST_OVER:
                    mActivtiy.mTestProcessHintTv.setText("测试终止");
                    break;
                case LISTENERING:
                    mActivtiy.mTestProcessHintTv.setText(R.string.test_paly_listen);
                    break;
                case RESET:
                    if (VinBean.getVin() != null) {
                        mActivtiy.mVinHintTv.setText("VIN:" + VinBean.getVin());
                    } else {
                        mActivtiy.mTestStateHintTv.setText(R.string.startdetection);
                        mActivtiy.mVinHintTv.setText(R.string.vinmsg_wait);
                    }
                    mActivtiy.mTestProcessHintTv.setText("等待检测");
                    break;
                case TOAST:
                    if (msg.arg1 == 0) {
                        Util.toast(msg.obj.toString());
                    } else {
                        Util.toast(msg.obj.toString(), msg.arg1);
                    }
                    break;
                case TEST_GET_VIN:
                    Log.e(TAG, "=====handleMessage:获取到新的vin:" + VinBean.getVin());
                    mActivtiy.mVinHintTv.setText("VIN:" + VinBean.getVin());
                    mActivtiy.initTimer();
                    break;
                case CHECKIN_INDEX:
                    //					mActivtiy.mTestProcessHintTv.setText((String.format(mActivtiy.getString(R.string.test_paly),
                    //							msg.obj.toString().replace(
                    //									DataVerification.MSG_CHECKIN_INDEX, "")
                    //									+ "", "s")));
                    break;
                case START_ANOTHER_TEST:
                    //停止之前的vin检测，开始新vin的检测
                    mActivtiy.stopCurrentTest();
                    break;
            }
        }
    }

    ;

    /**
     * 检测是否可以开始检测，蓝牙未连接，以及vin为空均不能开始
     *
     * @return
     */
    private synchronized boolean canStart() {
        //耳机蓝牙是否连接上
        final boolean isHeadSetConnect = Util.isHeadSetConnect();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isHeadSetConnect) {
                    mTestProcessHintTv.setText(R.string.state_wait_buletooth);
                } else if (VinBean.getVin() == null) {
                    mTestProcessHintTv.setText(R.string.state_wait_vin);
                }
            }
        });
        /**
         * Must wait for VIN before sSocket to buletooth(car)
         */
        return mTestThread == null && (isHeadSetConnect && VinBean.getVin() != null);
    }

    //使用设备自带相机进行扫描获取vin码的回调
    @Override
    protected void onActivityResult(int arg0, int arg1, final Intent arg2) {
        super.onActivityResult(arg0, arg1, arg2);
        switch (arg0) {
            case 0:
                if (arg2 != null && arg2.hasExtra("code")
                        && !arg2.getStringExtra("code").equals("")) {
                    String vinCode = arg2.getStringExtra("code");
                    vinCode = vinCode.substring(0, 17);
                    Log.e(TAG, "====通过扫码获取到了vin" + vinCode);
                    if (vinCode.length() == 17) {
                        VinBean.setVin(vinCode);
                        mVinHintTv.setText("VIN:" + VinBean.getVin());
                        Log.e(TAG, "====通过扫码获取到了vin");
                        initTimer();
                    } else {
                        toast("VIN码不正确");
                    }
                } else {
                    toast("扫描结果异常");
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View view) {
        if (view == mSettingsLayout) {
            Intent intent = new Intent(mContext, SettingActivity.class);
            startActivity(intent);
        } else if (view == mStartTestBtn) {
            if (isTestOver) {
                Intent intent = new Intent(mContext, ScanCaptureActivity.class);
                startActivityForResult(intent, 0);
            } else {
                toast("检测中，请检测完成后操作");
            }
        } else if (view == mCancelLayout) {
            if (!isTestOver) {
                stop();
            } else {
                onBackPressed();
            }
        } else if (view == mRestartBtn) {
            //软件重启
            Util.reSatrtApp(MainHomeActivtiy.this);
            isReStart = true;
        }
    }

    private List<TestInfoDTO> infoDTOs;


    public void queryVin() {
        DBManager dbManager = new DBManager(this);
        infoDTOs = dbManager.query();
        Collections.reverse(infoDTOs);
        String vin = null;
        if (infoDTOs != null && infoDTOs.size() != 0) {
            vin = infoDTOs.get(0).getVin();
        }
        if (vin != null) {
            Log.d("Vin码----》", vin);
            VinBean.setVin(vin);
            mVinHintTv.setText(vin);
        }
    }

    @Override
    public void onBackPressed() {
        Builder builder = new Builder(this);
        builder.setMessage("是否退出检测系统");
        builder.setTitle("提示");
        builder.setOnKeyListener(CarApp.keylistener).setCancelable(false);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
                stopThread();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @SuppressLint("Wakelock")
    @Override
    public void onDestroy() {
        stop();
        stopThread();
        mHandler.removeCallbacksAndMessages(null);//防止内存泄露
        mHandler = null;//置空，能防止内存泄露？
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        mAudioManager = null;
        mContext = null;
        LogcatHelper.getInstance(this).stop();
        mSpeechListenner.destroy();
        super.onDestroy();
    }

    /**
     * 停止各种线程
     *
     * @author:guokailin time:2017/6/8 10:06
     */
    private void stopThread() {
        //关闭连接服务端线程
        if (sConnectServerThread != null) {
            sConnectServerThread.interrupt();
            sConnectServerThread.kill();
            sConnectServerThread = null;
        }
    }

    private synchronized void reset() {
        Log.e(TAG, "reset: =========开始重置");
        BluetoothConnectActivityReceiver.cancel();
        //如果在TestThread还未检测完成就又来一个vin，则将上一个置空
        VinBean.setLastVin(null);
        isGetSecondVin = false;

        //		initListenerOk=false;
        sListenResult = false;
        sIsSendResultSuccess = false;
        isTestOver = true;

        openTimes = 0;
        listenIndex = 0;
        startTime = 0;

        mTestThread = null;

        //设置了检测超时时执行stop方法,在这里要去掉
        mHandler.removeMessages(TEST_OVER);
        mHandler.removeMessages(UPDATE_ANIMATION);
        //重置ui
        mHandler.sendEmptyMessage(RESET);
        //		mHandler.sendEmptyMessageDelayed(CHECK_START,2000);
        if (mTimer!=null){
            mTimer.cancel();
            mTimer = null;
        }

    }

    /**
     * 断开蓝牙连接
     */
    private synchronized void cancelBond(String currentTestVin) {
        Log.e(TAG, "cancelBond: ===============开始断开蓝牙连接");
        boolean isOk = currentTestVin != null && sListenResult;
        if (isOk) {
            Util.addTest(true, true, true);
        } else {
            boolean getVin = VinBean.getVin() != null;
            boolean getSound = Util.isHeadSetConnect();
            Util.addTest(false, getVin, getSound);
        }
        try {
            Util.cancelBond();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 进行蓝牙检测的线程
     *
     * @author:guokailin time:2017/6/8 10:06
     */
    private static class TestThread extends Thread {

        MainHomeActivtiy mActivtiy;
        WeakReference<MainHomeActivtiy> mWeakReference;

        public TestThread(String name, MainHomeActivtiy activtiy) {
            super(name);
            mWeakReference = new WeakReference<>(activtiy);
            mActivtiy = mWeakReference.get();
        }

        private String TAG = "TestThread";
        private String currentTestVin = null;//记录当前检测的vin

        @Override
        public void run() {
            //一旦开始检测，就记录下当前的vin
            currentTestVin = VinBean.getVin();
            isTestOver = false;//检测完毕标记置为false
            sListenResult = false;
            count = 3;
            //超时设置,要在检测时间内检测完成
            startTime = System.currentTimeMillis();
            //如果耳机蓝牙没有连接上或者已经检测完成了就不执行了
            if (!Util.isHeadSetConnect() || mActivtiy.isStopTestThread()) {
                Log.e(TAG, "run: =====================不执行检测");
                return;
            }
            //开启动画
            Log.e(TAG, "run: =======更新ui开始检测动画==1");
            mActivtiy.mHandler.sendEmptyMessage(StateInfo.START_ANIMATION);
            // 播放准备检测的动画
            while (count > 0) {
                Log.e(TAG, "run: ======循环更新ui检测进度和状态==2");
                mActivtiy.mHandler.sendEmptyMessage(StateInfo.UPDATE_ANIMATION);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // 等待初始化OK,如果初始化失败就不继续执行了
            if (!initListenerOk) {
                Log.e(TAG, "=================================wait for initListenerOk");
                return;
            }
            //开启声音检测
            Log.e(TAG, "run: ===检测线程开始声音检测");
            mActivtiy.openSCO();
            //在执行讯飞检测语音方法之前都睡眠1秒，防止没有开启就检测导致连接的是手机的a2dp通道
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mActivtiy.mSpeechListenner.startListen();//开始检测
            Log.e(TAG, "run: ===更新ui状态为声音检测中==3");
            mActivtiy.mHandler.sendEmptyMessage(StateInfo.LISTENERING);
            // 等待语音识别结果
            while (!sListenResult && !isTestOver) {
                if (mActivtiy.isStopTestThread() || !Util.isHeadSetConnect()) {
                    Log.e(TAG, "run: =========循环检测while已经开始，由于某种原因中断while");
                    break;
                }
                for (int i = 0; i < 3; i++) {
                    if (isTestOver && sListenResult) {
                        Log.e(TAG, "run: ==================等待检测结果");
                        break;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (!isTestOver || !sListenResult) {
                    mActivtiy.mSpeechListenner.cancel();
                    mActivtiy.closeSCO();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mActivtiy.openSCO();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mActivtiy.mSpeechListenner.startListen();//开始检测
                }
                for (int i = 0; i < 5; i++) {
                    if (isTestOver && sListenResult) {
                        Log.e(TAG, "run: ======================等待检测结果");
                        break;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (sListenResult) {
                mActivtiy.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mActivtiy.toast("声音检测：OK");
                    }
                });
                Log.e(TAG, "run: ===更新ui状态为声音检测完成==4");
                mActivtiy.mHandler.sendEmptyMessage(StateInfo.LISTENER_OVER);
            } else {
                Log.e(TAG, "run: ==========================声音检测失败");
                mActivtiy.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mActivtiy.toast("声音检测：NG");
                    }
                });
            }
            mActivtiy.mHandler.sendEmptyMessage(StateInfo.TEST_OVER);
            //如果是连续两个从服务器vin过来了，则finish方法不用重复执行了
            if (isGetSecondVin) {
                isGetSecondVin = false;
                return;
            }
            Log.e(TAG, "run: ====检测线程执行完成，开始执行finishRun");
            mActivtiy.finishRun(currentTestVin);//停止检测
        }

    }

    //蓝牙Sco通道建立
    private synchronized void openSCO() {
        if (mAudioManager != null && mAudioManager.isBluetoothScoOn()) {
            sScoIsOpen = true;
            Log.e(TAG, "openSCO: =====================sco通道已经打开");
            return;
        }
        Log.e(TAG, "openSCO=================================开始打开SCO");
        // 蓝牙录音的关键，启动SCO连接，耳机话筒才起作用
        mAudioManager.setBluetoothScoOn(true);
        mAudioManager.startBluetoothSco();
        sScoIsOpen = true;
    }

    private static boolean sScoIsOpen = false;// sco通道是否打开,默认没有打开

    //关闭Sco通道
    private synchronized void closeSCO() {
        Log.e(TAG, "closeSCO: =====关闭sco通道==通道是否打开了：" + sScoIsOpen);
        if (mAudioManager != null && mAudioManager.isBluetoothScoOn()) {
            mAudioManager.setBluetoothScoOn(false);
            mAudioManager.stopBluetoothSco();
            sScoIsOpen = false;
            Log.e(TAG, "closeSCO: ============================sco通道关闭了");
        }
        runOnUiThread(new Runnable() {
            public void run() {
                Log.e(TAG, "closeSCO==runOnUiThread: ================停止检测动画");
                stopAnim(mProgressImg);
            }
        });
    }

    /**
     * 判断sco通道是否已经打开
     *
     * @author:guokailin time:2017/6/27 9:25
     */
    private boolean isBluetoothScoOn() {
        boolean isOn = mAudioManager != null && mAudioManager.isBluetoothScoOn();
        Log.e(TAG, "isBluetoothScoOn: ==========检查sco通道是否打开：" + isOn);
        return isOn;
    }

    //标记是否是在第一个vin没检测完成就又来一个vin
    private static boolean isGetSecondVin = false;

    /**
     * 只要有新来的vin就停止当前的检测
     */
    private synchronized void stopCurrentTest() {
        Log.e(TAG, "=====================stopCurrentTest:立即停止当前的检测");
        isTestOver = true;
        isGetSecondVin = true;
        finishRun(VinBean.getLastVin());
    }
    //得到配对的设备列表，清除已配对的设备
    public void removePairDevice() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            //mBluetoothAdapter初始化方式
            //这个就是获取已配对蓝牙列表的方法
            Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : bondedDevices) {
                Log.e(TAG, "removePairDevice: "+device.getAddress());
                //这里可以通过device.getName()  device.getAddress()来判断是否是自己需要断开的设备
                unpairDevice(device);
            }
        }
    }

    //反射来调用BluetoothDevice.removeBond取消设备的配对
    private void unpairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass().getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            Log.e("mate", e.getMessage());
        }
    }
    /**
     * 停止检测
     */
    private void finishRun(final String currentTestVin) {
        Log.e(TAG, "finishRun:======检测线程结束检测");
        mHandler.removeMessages(TEST_OVER);
        if (!Util.isHeadSetConnect()) {
            if (isGetSecondVin && currentTestVin != null) {
                //如果上个vin还没开始检测，直接返回结果不合格
                sConnectServerThread.send(currentTestVin + "BLNC", "BL");
                Log.e(TAG, "finishRun:============" + currentTestVin + "直接返回测试结果NC");
            }
            Util.cancelBond();
            VinBean.setLastVin(null);
            reset();
            return;
        }
        /**
         * 处理测试过中，可能异常退出的情况，即关闭听/放语音，删除语音识别runnable<BR>
         * 如果是重新开始测试，则不需要发送结果给PC，且不需要保持测试结果
         * */
        /*if (listenSound != null) {
            listenSound.close();
			listenSound = null;
		}*/
        mHandler.sendEmptyMessage(StateInfo.SEND_RESULT);
        if (currentTestVin != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "finishRun:============发送：" + currentTestVin + ":的测试结果=" + (sListenResult ? "BLOK" : "BLNG"));
                    sConnectServerThread.send(sListenResult ? currentTestVin + "BLOK" : currentTestVin + "BLNG", "BL");
                }
            }).start();
        }
        closeSCO();
        //按正常检测流程来检测完了之后重置当前vin,如果连续来了两个vin，则不重置最后一个
        if (!isGetSecondVin) {
            VinBean.setVin(null);
        }
        cancelBond(currentTestVin);
//        removePairDevice();
        //上传检测结果到服务器
        //需要保存测试结果
        save(currentTestVin);
        //释放资源，对象置空
        mHandler.sendEmptyMessage(RELEASE);
    }

    private void save(String vin) {
        if (vin == null) {
            return;
        }
        Log.e(TAG, "save: ================保存检测结果到本地:" + vin);
        ArrayList<TestInfoDTO> persons = new ArrayList<>();
        TestInfoDTO dto = new TestInfoDTO(vin,
                new Date().getTime(), Configuration.CHECK_TRUE,
                sListenResult ? Configuration.CHECK_TRUE
                        : Configuration.CHECK_FLASE,
                sIsSendResultSuccess ? Configuration.UPLOAD_TURE
                        : Configuration.UPLOAD_FLASE, 0);
        persons.add(dto);
        sDBManager.add(persons);
    }

    /**
     * handler发送指令
     *
     * @type 指令
     */
    public void sendMessage(int type) {
        Message message = mHandler.obtainMessage(type);
        mHandler.sendMessage(message);
    }

    public void sendMessageDelayed(int type, long time) {
        Message message = mHandler.obtainMessage(type);
        mHandler.sendMessageDelayed(message, time);
    }

    /**
     * 开始检测
     */
    private synchronized void startTest() {
        if (mTestThread != null) {
            Log.e(TAG, "=================you must stop before run");
        } else {
            mTestThread = new TestThread("TestThread", this);
            mTestThread.start();
        }
    }

    /**
     * 开始刷新动画
     *
     * @param :imageView刷新图标
     */
    private void startAnim(ImageView imageView) {
        if (operatingAnim != null) {
            imageView.startAnimation(operatingAnim);
        }
    }

    /**
     * 停止刷新动画
     *
     * @param :imageView刷新图标
     */
    private void stopAnim(ImageView imageView) {
        imageView.clearAnimation();
    }

    /**
     * 停止检测
     */
    private synchronized void stop() {
        Log.e(TAG, "stop: ==============================停止检测");
        isTestOver = true;
        finishRun(VinBean.getVin());
    }

    /**
     * 是否中断检测线程
     *
     * @return
     */
    private boolean isStopTestThread() {
        //如果检测完毕则中断线程
        if (isTestOver) {
            Log.e(TAG, "isStopTestThread: =================检测完毕中断检测线程isTestOver" + isTestOver);
            return true;
        }
        //如果检测超时则中断线程
        final long now = System.currentTimeMillis();
        if (startTime > 0 && (now - startTime) > MAX_TEST) {
            Log.e(TAG, "isStopTestThread: =====================检测超时中断检测线程");
            isTestOver = true;
            return true;
        } else {
            Log.e(TAG, "isStopTestThread: ======================不中断检测线程");
            return false;
        }
    }

    private Toast mToast;

    private void toast(String msg) {
        //		mHandler.obtainMessage(TOAST, msg).sendToTarget();
        if (mToast == null) {
            mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        }
        mToast.setText(msg);
        mToast.show();
    }

    /**
     * 连接服务器的线程
     *
     * @author:guokailin time:2017/6/8 14:45
     */
    public static class ConnectServerThread extends Thread {
        //是否将结果本地保存的检测结果上传到服务器
        public static boolean isUploaded = false;
        private final static String START = "RR==";
        private final static String END = "[END]";
        private static final String TAG = "ConnectServerThread";

        private static Socket sSocket;
        private static InputStream sInputStream;
        private static OutputStream sOutputStream;

        private final static int SEND_TIMES = 3;
        private final static int SEND_TIMEOUT = 3 * 1000;
        private static final long TIMEOUT = 5 * 1000;
        private static String mReply = null;
        private static boolean isOver = false;

        private static String bvin, bsend, breply;

        WeakReference<MainHomeActivtiy> mWeakReference;
        MainHomeActivtiy mMainHomeActivtiy;

        public ConnectServerThread(MainHomeActivtiy activtiy) {
            super();
            mWeakReference = new WeakReference<>(activtiy);
            mMainHomeActivtiy = mWeakReference.get();
            //			Log.e(TAG, "xxx====ConnectServerThread: =====创建连接服务器线程");
        }

        private static Object lock = new Object();

        @Override
        public void run() {
            Log.e(TAG, "xxx===run:=============连接服务器线程开始运行");
            while (!Thread.interrupted()) {
                if (createConnect()) {
                    Log.e(TAG, "run:====================成功连接服务器");
                    try {
                        sSocket.sendUrgentData(0xFF);
                        /*synchronized (lock) {
                            Log.e(TAG, "===========wait for 已上锁，等待中...");
							lock.wait();
						}*/
                    } catch (IOException e) {
                        e.printStackTrace();
                        kill();
                    } /*catch (InterruptedException e) {
                        e.printStackTrace();
					}*/
                }
                try {
                    Thread.sleep(TIMEOUT);//睡眠一会儿再连接
                    //					Log.e(TAG, "run: =====创建连接的线程进入睡眠中");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.e(TAG, "run: =========连接服务器线程退出循环,不在连接服务器");
            kill();
        }

        /**
         * 建立socket连接
         *
         * @return
         * @throws Exception
         */
        private synchronized boolean createConnect() {
            Log.e(TAG, "createConnect:============创建连接");
            if (sSocket != null && !sSocket.isConnected()) {
                Log.e(TAG, "createConnect: ====socket不为空，但没有连接上服务器");
                kill();
            }
            if (sSocket == null) {
                String ip = NetConfig.getIP();
                int port = NetConfig.getPORT();
                if (ip == null) {
                    Log.e(TAG, "createConnect:========ip为空，请设置ip");
                    return false;
                }
                try {
                    sSocket = new Socket(InetAddress.getByName(ip), port);
                    Log.e(TAG, "createConnect========连接到服务器:" + ip + ":" + port);
                    sSocket.setKeepAlive(true);
                    sInputStream = sSocket.getInputStream();
                    sOutputStream = sSocket.getOutputStream();
                    new ReadThread().start();//开启读取线程
                    sSocket.setTcpNoDelay(true);
                    Log.e(TAG, "=========createConnect: 成功连接上服务器啦，好开心...");
                    return true;
                } catch (IOException e) {
                    Log.e(TAG, "createConnect: ========连接服务器失败===");
                    e.printStackTrace();
                }
            }
            return false;
        }

        /**
         * 读取线程，从服务端读取vin码
         *
         * @author:guokailin time:2017/6/8 10:25
         */
        private class ReadThread extends Thread {
            @Override
            public void run() {
                //				Log.e(TAG, "run: =====================读取服务器数据的线程运行了");
                byte[] buffer = new byte[128];
                int len;
                while (sInputStream != null) {
                    //					Log.e(TAG, "run:====开始从服务器读取数据啦");
                    try {
                        len = sInputStream.read(buffer);
                        if (len > 0) {
                            final String read = new String(buffer, 0, len);
                            processRead(read);
                            Log.e(TAG, "run:====================从服务器读取到数据vin:" + read);
                        } else {
                            Log.e(TAG, "=============================sSocket is close!");
                            break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                }
                kill();
            }
        }

        /**
         * 处理从服务器数据，获取到vin码
         *
         * @author:guokailin time:2017/6/8 11:41
         */
        private void processRead(String read) {
            if (read.startsWith(START) && read.endsWith(END)) {
                read = read.replace(START, "").replace(END, "").trim();
                if (read.length() == 17) {
                    //如果当前待检测vin为空,则立即检测
                    if (VinBean.getVin() == null) {
                        VinBean.setVin(read);
                        android.util.Log.w(TAG, "get VIN:" + read);
                        mMainHomeActivtiy.sendMessage(StateInfo.TEST_GET_VIN);
                    } else {
                        //如果当前vin正在检测或检测还没开始，则中止当前检测，开始检测新的vin
                        VinBean.setLastVin(VinBean.getVin());
                        VinBean.setVin(read);//将新的vin设置进去
                        mMainHomeActivtiy.sendMessage(StateInfo.START_ANOTHER_TEST);
                        mMainHomeActivtiy.sendMessageDelayed(StateInfo.TEST_GET_VIN, 1500);
                        Log.e(TAG, "processRead:新来的vin=" + VinBean.getVin() + "\t原来的vin" + VinBean.getLastVin());
                    }
                    Util.cancelBond();
                } else if (read.equals(mReply)) {
                    android.util.Log.w(TAG, "get reply:" + read);
                    mReply = null;
                } else if (read.equals("END")) {//检测任务完毕
                    android.util.Log.w(TAG, "get reply:" + read);
                    send("END", null);
                    mMainHomeActivtiy.sendMessage(StateInfo.TEST_OVER);
                }
            } else {//vin码发送错误
                android.util.Log.e(TAG, "vin error:" + read);
                send(read, null);
            }
        }

        /**
         * reply为空，要么检测任务结束，要么是服务器发的vin码不对
         * 向服务器发送检测结果
         *
         * @author:guokailin time:2017/6/8 11:40
         */
        public void send(String string, String reply) {
            int times = SEND_TIMES;
            if (!string.startsWith(START)) {
                string = START + string;
            }
            if (!string.endsWith(END)) {
                string = string + END;
            }
            mReply = reply;
            //重复发送三次
            while (times-- > 0) {
                try {
                    sOutputStream.write(string.getBytes());
                    sOutputStream.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                    if (!isOver) {
                        bsend = string;
                        breply = reply;
                        bvin = VinBean.getVin();
                        isOver = true;
                    }
                    kill();
                    return;
                }
                try {
                    Thread.sleep(SEND_TIMEOUT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (reply == null || mReply == null) {
                    break;
                }
            }
            if (reply != null) {
                //是否上传了本地保存的检测结果
                if (isUploaded) {
                    Toast.makeText(mContext, mReply == null ? "上传成功" : "上传失败",
                            Toast.LENGTH_SHORT).show();
                } else if (!isTestOver) {
                    if (mReply != null) {
                        mMainHomeActivtiy.sendMessage(StateInfo.SEND_NG);
                        Log.e(TAG, "监测结果上传失败 ");
                    } else {
                        mMainHomeActivtiy.sendMessage(StateInfo.SEND_OK);
                        Log.e(TAG, "监测结果上传成功 ");
                    }
                }
            }
        }

        /**
         * 断开连接，关闭流
         *
         * @author:guokailin time:2017/6/8 9:59
         */
        public void kill() {
            try {
                if (sInputStream != null) {
                    sInputStream.close();
                }
                if (sOutputStream != null) {
                    sOutputStream.close();
                }
                if (sSocket != null) {
                    sSocket.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            sInputStream = null;
            sOutputStream = null;
            sSocket = null;
            Log.e(TAG, "kill================断开连接，关闭流,对象置空: ");
            synchronized (lock) {
                lock.notify();
                Log.e(TAG, "==============notify 释放锁...");
            }
            try {
                this.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
