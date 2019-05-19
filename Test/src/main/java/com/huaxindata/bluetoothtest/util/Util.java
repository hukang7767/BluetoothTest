package com.huaxindata.bluetoothtest.util;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import com.huaxindata.bluetoothtest.CarApp;
import com.huaxindata.bluetoothtest.activity.MainHomeActivtiy;
import com.huaxindata.bluetoothtest.entity.BleDevices;
import com.iflytek.cloud.SpeechUtility;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.List;

public class Util {

    public static final String TAG = "Util";

    /**
     * 测试成功 addTest(true,true,true);
     * 没有连接蓝牙 addTest(false,true,false);
     * 连接早了，没有VIN addTest(false,false,true);
     * 声音识别失败 addTest(false,false,false);
     *
     * @param isok
     * @param getVin
     * @param getSound
     */
    public static void addTest(boolean isok, boolean getVin, boolean getSound) {
        if (CarApp.app == null) {
            return;
        }
        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        final SharedPreferences pref = CarApp.app.getSharedPreferences("total_" + year, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        final long oldSum = pref.getLong("total:" + dayOfYear + "sum", 0);
        final long oldOk = pref.getLong("total:" + dayOfYear + "ok", 0);
        final long oldVin = pref.getLong("total:" + dayOfYear + "vin", 0);
        final long oldSound = pref.getLong("total:" + dayOfYear + "sound", 0);
        /**
         * 如果测试成功，则纪录一次总个数以及成功的次数；
         */
        editor.putLong("total:" + dayOfYear + "sum", oldSum + 1);
        if (isok) {
            editor.putLong("total:" + dayOfYear + "ok", oldOk + 1);
        } else {
            if (getVin && getSound) {
                //editor.putLong("total:" + dayOfYear + "ok", oldOk++);
            } else if (getVin) {
                editor.putLong("total:" + dayOfYear + "vin", oldVin + 1);
            } else if (getSound) {
                editor.putLong("total:" + dayOfYear + "sound", oldSound + 1);
            }
        }
        editor.apply();
    }

    public static String getTestInfo() {
        if (CarApp.app == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR) - 1;
        final SharedPreferences pref = CarApp.app.getSharedPreferences("total_" + year, Context.MODE_PRIVATE);

        if (dayOfYear <= 0) {
            return null;
        }

        long oldSum = pref.getLong("total:" + dayOfYear + "sum", 0);
        long oldOk = pref.getLong("total:" + dayOfYear + "ok", 0);
        long oldVin = pref.getLong("total:" + dayOfYear + "vin", 0);
        long oldSound = pref.getLong("total:" + dayOfYear + "sound", 0);

        StringBuilder sb = new StringBuilder();

        sb.append("昨天共测试:");
        sb.append(oldSum);
        sb.append(" 成功:");
        sb.append(oldOk);

        sb.append(" 未连接蓝牙:");
        sb.append(oldVin);

        sb.append(" 连接蓝牙过早,未收到VIN:");
        sb.append(oldSound);

        sb.append(" 识别失败:");
        sb.append(oldSum - oldOk - oldVin - oldSound);

        return sb.toString();
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            State wifi = connectivity.getNetworkInfo(
                    ConnectivityManager.TYPE_WIFI).getState();// WIFI网络连接状态

            if (wifi == State.CONNECTED || wifi == State.CONNECTING) {
                // LogInfo.d("isNetworkAvailable", "NETWORK_WIFI");
                return true;
            }
        }
        // LogInfo.e("isNetworkAvailable", "NETWORK_NOTWIFI");
        return false;
    }

    public static String NetworkSetting(Context context) {
        // 获取wifi服务
        WifiManager wifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        // 判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
            return "";
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return intToIp(ipAddress);
    }

    public static String NetworkIp(Context context) {
        // 获取wifi服务
        WifiManager wifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        // 判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
            return "";
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return intToIps(ipAddress);
    }

    private static String intToIp(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
                + "." + (i >> 24 & 0xFF);
    }

    private static String intToIps(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
                + ".";
    }

    // 通过Service的类名来判断是否启动某个服务
    public static boolean ServiceIsStart(Context context, String className) {
        ActivityManager manager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (className.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void execShellCmd(String cmd) {
        try {
            // 申请获取root权限，这一步很重要，不然会没有作用
            Process process = Runtime.getRuntime().exec("su");
            // 获取输出流
            OutputStream outputStream = process.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(
                    outputStream);
            dataOutputStream.writeBytes(cmd);
            dataOutputStream.flush();
            dataOutputStream.close();
            outputStream.close();
        } catch (Throwable t) {
            t.printStackTrace();
            Toast.makeText(CarApp.app, "尝试获取Root权限", Toast.LENGTH_LONG).show();
        }
    }

    public static void execCmd(String cmd) {
        try {
            // 申请获取root权限，这一步很重要，不然会没有作用
            Process process = Runtime.getRuntime().exec(cmd);
            // 获取输出流
            if (process != null) {
                process.destroy();
                process = null;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static String getTopAppName(Context context) {
        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        return cn.getClassName();
    }

    public static boolean setTrust(Class<?> btClass, BluetoothDevice device)
            throws Exception {
        Method setTrust = btClass.getMethod("setTrust", Boolean.class);
        Boolean returnValue = (Boolean) setTrust.invoke(device, true);
        return returnValue.booleanValue();
    }

    /**
     * 初始画语言服务，检测是否按照讯飞语言+服务
     */
    public static boolean initSpeech(Context context) {
        if (!SpeechUtility.getUtility().checkServiceInstalled()) {
            // 注意此处要放在show之后 否则会报异常
            String url = SpeechUtility.getUtility().getComponentUrl();
            String assetsApk = "SpeechService.apk";
            if (processInstall(url, assetsApk)) {
                return true;
            }
        } else {
            return true;
        }
        return false;
    }

    // 判断手机中是否安装了讯飞语音+
    private static boolean checkSpeechServiceInstall() {
        String packageName = "com.iflytek.vflynote";
        List<PackageInfo> packages = CarApp.app.getPackageManager()
                .getInstalledPackages(0);
        for (int i = 0; i < packages.size(); i++) {
            PackageInfo packageInfo = packages.get(i);
            if (packageInfo.packageName.equals(packageName)) {
                System.out.println(packageInfo.versionName + ":"
                        + packageInfo.versionCode);
                return true;
            } else {
                continue;
            }
        }
        return false;
    }

    /**
     * 如果服务组件没有安装，有两种安装方式。 1.直接打开语音服务组件下载页面，进行下载后安装。
     * 2.把服务组件apk安装包放在assets中，为了避免被编译压缩，修改后缀名为mp3，然后copy到SDcard中进行安装。
     */
    private static boolean processInstall(String url, String assetsApk) {
        // 直接下载方式
        // ApkInstaller.openDownloadWeb(context, url);
        // 本地安装方式
        if (!ApkInstaller.installFromAssets(CarApp.app, assetsApk)) {
            Toast.makeText(CarApp.app, "安装失败", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public static boolean isHeadSetConnect() {
        boolean flag1 = false;
        boolean flag2 = false;
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        /*try {
            final int state = adapter.getProfileConnectionState(BluetoothProfile.HEADSET);
            switch (state) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.e(TAG, "isHeadSetConnect:1 =================蓝牙耳机已连接");
                    flag1=true;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e(TAG, "isHeadSetConnect: 1=================蓝牙耳机已断开连接");
                    flag1=false;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }*/
        try {
            final Method getConnectionState = BluetoothAdapter.class.getDeclaredMethod("getConnectionState");
            final int state = (int) getConnectionState.invoke(adapter);
            switch (state) {
                case BluetoothAdapter.STATE_CONNECTED:
                    Log.e(TAG, "isHeadSetConnect：2====================蓝牙已经连接");
                    flag2 = true;
                    break;
                case BluetoothAdapter.STATE_DISCONNECTED:
                    Log.e(TAG, "isHeadSetConnect：0====================蓝牙未连接");
                    flag2 = false;
                    break;
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return flag2;
    }

    @SuppressLint("DefaultLocale")
    public static String getDeviceName(BluetoothDevice device) throws Exception {
        String result = "bluetooth_a2dp_sink_priority_"
                + device.getAddress().toUpperCase();
        return result;
    }

    /**
     * 取消连接
     *
     * @throws Exception
     */
    public static void cancelBond() {
        Log.e(TAG, "cancelBond====================执行断开蓝牙的连接方法");
        Method cancelBond = null;
        try {
            cancelBond = BluetoothDevice.class.getDeclaredMethod("removeBond");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        boolean ret = false;
        final boolean[] isEnable = new boolean[]{true, true};
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        adapter.getProfileProxy(CarApp.app, new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                if (profile == BluetoothProfile.HEADSET) {
                    Method deleteBond = null;
                    try {
                        deleteBond = BluetoothDevice.class.getDeclaredMethod("removeBond");
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                    final List<BluetoothDevice> connectedDevices = proxy.getConnectedDevices();
                    for (BluetoothDevice device : connectedDevices) {
                        try {
                            final boolean ret = (boolean) deleteBond.invoke(device);
                            if (!ret) {
                                isEnable[1] = ret;
                            }
                            Log.e(TAG, "cancelBond:3========断开已经连接的蓝牙" + device.getAddress() + ":" + ret);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onServiceDisconnected(int profile) {

            }
        }, BluetoothProfile.HEADSET);
        for (BluetoothDevice device : adapter.getBondedDevices()) {
            try {
                ret = (boolean) cancelBond.invoke(device);
                if (!ret) {
                    isEnable[0] = ret;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            Log.e(TAG, "cancelBond:1========断开已经连接的蓝牙" + device.getAddress() + ":" + ret);
        }
        String mac = BleDevices.getCurrentPairedMac();
        if (!("".equals(mac) || mac.length() == 0)) {
            final BluetoothDevice remoteDevice = adapter.getRemoteDevice(mac);
            if (remoteDevice != null) {
                try {
                    ret = (boolean) cancelBond.invoke(remoteDevice);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                BleDevices.setCurrent_Paired_mac("");
                Log.e(TAG, "cancelBond:2========断开已经连接的蓝牙" + remoteDevice.getAddress() + ":" + ret);
            }
        }
        if (!(isEnable[0] & isEnable[1])) {//如果断不开就开启一个线程关闭蓝牙然后再打开蓝牙
            new Thread() {
                @Override
                public void run() {
                    Log.e(TAG, "cancelBond: ===================关闭蓝牙再打开");
                    adapter.disable();
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    adapter.enable();
                }
            }.start();
        }
        final Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec("pm clear com.android.bluetooth");
            runtime.exec("rm /data/misc/bluedroid/*.*");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //复用toast对象，避免toast密集弹出，长时间不消失
    private static Toast sToast = null;

    public static void toast(String msg) {
        if (CarApp.app == null) {
            return;
        }
        if (sToast == null) {
            Toast.makeText(CarApp.app, "", Toast.LENGTH_SHORT);
        }
        sToast.setText(msg);
        sToast.show();
    }

    public static void toast(String msg, int duration) {
        if (CarApp.app == null) {
            return;
        }
        sToast.setDuration(duration);
        sToast.setText(msg);
        sToast.show();
    }
    /*public static void clearUserData(Context context) {
        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        Method[] methods = ActivityManager.class.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            Log.d("hebing", "i: " + i + " method name: " + methods[i].getName());
            if ("clearApplicationUserData".equals(methods[i].getName())) {
                try {
                    Object ret = methods[i].invoke(am,
                            "com.hx.shangde",
                            // context.getPackageName(),
                            new ClearUserDataObserver());
                    Log.i("hebing", "clearUserData:" + ret.toString());
                } catch (Exception e) {
                    Log.d("hebing", "clearUserData", e);
                }
                break;
            }
        }
    }*/

    /*final static class ClearUserDataObserver extends IPackageDataObserver.Stub {

        public void onRemoveCompleted(final String packageName,
                                      final boolean succeeded) {
            // final Message msg = mHandler.obtainMessage(CLEAR_USER_DATA);
            // msg.arg1 = succeeded?OP_SUCCESSFUL:OP_FAILED;
            // mHandler.sendMessage(msg);
            Log.w("hebing", "ClearUserDataObserver:" + succeeded);
        }
    }*/

    /**
     * 重启自身
     *
     * @param context
     */
    public static void reSatrtApp(Context context) {
        Intent mStartActivity = new Intent(context, MainHomeActivtiy.class);
        int mPendingIntentId = 87775236;
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }
}
