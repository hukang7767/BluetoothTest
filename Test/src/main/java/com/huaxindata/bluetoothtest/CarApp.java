package com.huaxindata.bluetoothtest;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.huaxindata.bluetoothtest.activity.SettingActivity;
import com.huaxindata.bluetoothtest.entity.NetConfig;
import com.huaxindata.bluetoothtest.entity.VinBean;
import com.huaxindata.bluetoothtest.util.Util;
import com.iflytek.cloud.SpeechUtility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CarApp extends Application {
    public static final String TAG="CarApp";

    public static final String APP_ID = "appid=";

    public static CarApp app;
    public static SharedPreferences mPreferences;

    @Override
    public void onCreate() {
        // 应用程序入口处调用，避免手机内存过小，杀死后台进程后通过历史intent进入Activity造成SpeechUtility对象为null
        // 如在Application中调用初始化，需要在Mainifest中注册该Applicaiton
        // 注意：此接口在非主进程调用会返回null对象，如需在非主进程使用语音功能，请增加参数：SpeechConstant.FORCE_LOGIN+"=true"
        // 参数间使用半角“,”分隔。
        // 设置你申请的应用appid,请勿在'='与appid之间添加空格及空转义符

        // 注意： appid 必须和下载的SDK保持一致，否则会出现10407错误
        SpeechUtility.createUtility(CarApp.this, APP_ID + getString(R.string.appid));
        app = this;
        initNetConfig();
        BluetoothAdapter.getDefaultAdapter().getProfileProxy(this, mProfileListener, BluetoothProfile.HEADSET);
        super.onCreate();
//        autoCloseActivity();
    }

    /**
     *
     *初始化网络配置
     *@author:guokailin
     *time:2017/6/8 13:51
     */
    private void initNetConfig() {
        mPreferences = getSharedPreferences("CarBluetoothApp", Context.MODE_PRIVATE);
        NetConfig.setIP(mPreferences.getString("IP", null));
        NetConfig.setPORT(mPreferences.getInt("PORT", 2223));
        NetConfig.setMaxTest(mPreferences.getInt("MAX_TIME", 45*1000));
        Log.e(TAG, "initNetConfig:====初始化网络配置,ip:"+NetConfig.getIP()+"==检测超时："+NetConfig.getMaxTest());
    }

    public static OnKeyListener keylistener = new OnKeyListener() {
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                return true;
            } else {
                return false;
            }
        }
    };

    public static BluetoothProfile mProfile;
    public static List<String> mMacList = new ArrayList<>();
    private BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.A2DP) {
                Log.e(TAG, "onServiceConnected============蓝牙a2dp设备");
            }
            if (profile == BluetoothProfile.GATT) {
                Log.e(TAG, "onServiceConnected============蓝牙Gatt设备");
            }
            if (profile == BluetoothProfile.GATT_SERVER) {
                Log.e(TAG, "onServiceConnected============蓝牙gattServer");
            }
            if (profile == BluetoothProfile.HEADSET) {
                Log.e(TAG, "onServiceConnected============蓝牙headset设备");
                mProfile = proxy;
            }
        }
        public void onServiceDisconnected(int profile) {
            Log.e(TAG, "onServiceDisconnected=========================蓝牙服务断开");
            if (profile == BluetoothProfile.HEADSET) {
                mProfile = null;
            }
        }
    };
    private static int count=0;
    public synchronized static void addMac(final String mac) {
        if (mMacList!=null&&mMacList.size()>30){
            mMacList.clear();
        }
        mMacList.add(mac);
        Log.e(TAG,"addmac=========================成功加入mac过滤器:"+mac);
    }
    public synchronized static boolean isContainMac(String mac){
        return mMacList.contains(mac);
    }
    /**
     * 是否允许连接，即如果上一个vin对应的中控已经检测成功并且加入到缓存列表中时
     * 又来一个同样的vin,则不允许连接，就是说，同样的vin对应的中控检测成功后不可连续检测
     * @param d
     * @return
     */
    public synchronized static boolean isAllowConnect(BluetoothDevice d) {

        if (isStopDeviceConnect(d)) {
            Log.e(TAG, "isAllowConnect: ======是否允许连接1："+false);
            return false;
        }
        if (Util.isHeadSetConnect()) {
            Log.e(TAG, "isAllowConnect: ======是否允许连接2："+false);
            return false;
        }
        Log.e(TAG, "isAllowConnect: ======是否允许连接3："+true);
        return true;
    }
    /**
     * 当mac地址不在过滤列表时，直接返回false表示允许连接
     * 当mac地址在过滤列表时，则获取与之对应的vin号，如果vin号不为空且vin号与新的vin号不相等时，返回false
     * 其它情况统一返回true表示阻止连接
     * @param d
     * @return
     */
    private static boolean isStopDeviceConnect(BluetoothDevice d) {
        if (VinBean.getVin() == null) {
            Log.e(TAG, "isStopDeviceConnect: vin为空==========请先获取vin");
            return true;
        }
        final String key = d.getAddress();

        if (mMacList.contains(key)) {
            Log.e(TAG, "isStopDeviceConnect: ===============此车已经测过，阻止连接");
            return true;
        }
        Log.e(TAG, "isStopDeviceConnect:====================此车未测过，不阻止连接");
        return false;
    }
    @Override
    public void onTerminate() {
        super.onTerminate();
        BluetoothAdapter.getDefaultAdapter().closeProfileProxy(
                BluetoothProfile.HEADSET, mProfile);
    }

}

