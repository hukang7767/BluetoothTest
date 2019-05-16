package com.huaxindata.bluetoothtest.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import com.huaxindata.bluetoothtest.CarApp;
import com.huaxindata.bluetoothtest.entity.BleDevices;
import com.huaxindata.bluetoothtest.entity.NetConfig;
import com.huaxindata.bluetoothtest.entity.VinBean;
import com.huaxindata.bluetoothtest.util.ClsUtils;
import com.huaxindata.bluetoothtest.util.Util;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

public class BluetoothConnectActivityReceiver extends BroadcastReceiver {
    static UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // String strPsw = "1212";
    // private static Context context;
    public static final String TAG = "BluetoothReceiver";
    public static ConnectThread connectThread;
    private Context mContext;
    private static BluetoothDevice remoteDevice;
    private AlertDialog dialog;


    @Override
    public void onReceive(Context context, Intent intent) {
        this.mContext = context;
        final String action = intent.getAction();
        final BluetoothDevice device = intent
                .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        Log.e(TAG, "广播内容:========================" + action);
        if (action.equals("android.bluetooth.device.action.PAIRING_REQUEST")) {
            //配对请求
            Log.e(TAG, "onReceive:========================请求配对设备：" + device.getAddress());
            //获取配对类型
            final int type = intent.getIntExtra(
                    BluetoothDevice.EXTRA_PAIRING_VARIANT,
                    BluetoothDevice.PAIRING_VARIANT_PASSKEY_CONFIRMATION);
            cancel();//取消connect线程
            switch (CarApp.isAllowConnectType(device)){
                case 1:
                    abortBroadcast();//如果没有将广播终止，则会出现一个一闪而过的配对框。
                    boolean confirmation = device.setPairingConfirmation(false);
                    for (int i = 0; i < 5; i++) {//发送上次，确保发送成功
                        if (confirmation) {
                            break;
                        }
                        confirmation = device.setPairingConfirmation(false);
                    }
                    break;
                case 2:
                    Toast.makeText(mContext,"该设备已经连接过",Toast.LENGTH_LONG).show();
                    break;
                case 3:
                    abortBroadcast();//如果没有将广播终止，则会出现一个一闪而过的配对框。
                    boolean confirmation1 = device.setPairingConfirmation(true);
                    for (int i = 0; i < 5; i++) {//发送上次，确保发送成功
                        if (confirmation1) {
                            break;
                        }
                        confirmation1 = device.setPairingConfirmation(true);
                    }
                    break;
            }
//            boolean isAllowConnect = CarApp.isAllowConnect(device);
//            if (isAllowConnect) {
//                abortBroadcast();//如果没有将广播终止，则会出现一个一闪而过的配对框。
//                pair(device, type, isAllowConnect);
//            } else {
//                Toast.makeText(mContext,"该设备已经连接过",Toast.LENGTH_LONG).show();
//                pair(device, type, isAllowConnect);
//            }
        } else if (action.equals("android.bluetooth.device.action.CONNECTION_ACCESS_REQUEST")) {
            Log.e(TAG, "onReceive:===================================33333333333333");
            sendReplyAndCancel(context, intent);
        } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            //监听网络变化

            ConnectivityManager manager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifi = manager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (wifi.isConnected()) {
                NetConfig.setIsNetworkOk(true);
            } else {
                NetConfig.setIsNetworkOk(false);
            }

        } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            final int anInt = intent.getExtras().getInt(BluetoothAdapter.EXTRA_STATE);
            switch (anInt) {
                case BluetoothAdapter.STATE_OFF:
                    Log.e(TAG, "EXTRA_STATE=======================蓝牙关闭");
                    break;
                case BluetoothAdapter.STATE_ON:
                    Log.e(TAG, "EXTRA_STATE=======================蓝牙打开");
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    Log.e(TAG, "EXTRA_STATE=======================蓝牙正在打开");
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    Log.e(TAG, "EXTRA_STATE=======================蓝牙正在关闭");
                    break;
            }
        } else if (action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) {
            final int connectionState = intent.getExtras().getInt(BluetoothAdapter.EXTRA_CONNECTION_STATE);
            switch (connectionState) {
                case BluetoothAdapter.STATE_CONNECTED:
                    BleDevices.setCurrent_Paired_mac(device.getAddress());
                    CarApp.addMac(device.getAddress());
                    Log.e(TAG, "BluetoothAdapter:xxxx================蓝牙已连接:" + device.getAddress());
                    break;
                case BluetoothAdapter.STATE_CONNECTING:
                    Log.e(TAG, "BluetoothAdapter:================蓝牙正在连接:" + device.getAddress());
                    break;
                case BluetoothAdapter.STATE_DISCONNECTED:
                    Log.e(TAG, "BluetoothAdapter:================蓝牙已断开连接:" + device.getAddress());
                    break;
                case BluetoothAdapter.STATE_DISCONNECTING:
                    Log.e(TAG, "onReceive:================蓝牙正在断开连接:" + device.getAddress());
                    break;
            }
//            afterFoundBtHartModem(device);
        } else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
            final int bondState = intent.getExtras().getInt(BluetoothDevice.EXTRA_BOND_STATE);
            switch (bondState) {
                case BluetoothDevice.BOND_BONDED:
                    CarApp.addMacAndVin(device.getAddress(),VinBean.getVin());
                    Log.e(TAG, "BluetoothDevice:BOND================蓝牙设备已配对:" + device.getAddress());
                    break;
                case BluetoothDevice.BOND_BONDING:
                    Log.e(TAG, "BluetoothDevice:================蓝牙设备正在请求连接或配对:" + device.getAddress());
                    break;
                case BluetoothDevice.BOND_NONE:

                    Log.e(TAG, "BluetoothDevice:================蓝牙设备请求连接或配对失败或已断开:" + device.getAddress());
                    break;
            }
//            afterFoundBtHartModem(device);
        }
    }

    private final byte[] defaulePin = "0000".getBytes();

    /**
     * 蓝牙配对
     *
     * @param device
     * @param type
     */
    @SuppressLint({"InlinedApi", "NewApi"})
    public void pair(BluetoothDevice device, int type, boolean isAllowConnect) {
        Log.i(TAG, "pair: "+type+"............."+isAllowConnect);
        switch (type) {
            //弹出对话框提示用户输入pin
            case BluetoothDevice.PAIRING_VARIANT_PIN:
                try {
                    final String vin;
                    if (VinBean.getVin() == null) {
                        vin = null;
                    } else {
                        /**
                         * 截取前5位VIN号用于判断
                         */
                        vin = CarApp.mPreferences.getString(VinBean.getVin().substring(0, 5), null);
                    }
                    if (vin != null) {
                        device.setPin(CarApp.mPreferences.getString(vin + "password", null).getBytes());
                    } else {
                        device.setPin(defaulePin);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            //弹出带有密码的对话框提示用户确认
            case BluetoothDevice.PAIRING_VARIANT_PASSKEY_CONFIRMATION:

                if(!isAllowConnect){

//                    buildIpDialog(device);
                }else {
                    boolean confirmation = device.setPairingConfirmation(isAllowConnect);
                    for (int i = 0; i < 5; i++) {//发送上次，确保发送成功
                        if (confirmation) {
                            break;
                        }
                        confirmation = device.setPairingConfirmation(isAllowConnect);
                    }

                }
                Log.e(TAG, "pair:=========回复连接请求:" + isAllowConnect + "===连接请求发送是否成功：" );
                break;
        }

    }
    private void buildIpDialog(final BluetoothDevice device) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                MainHomeActivtiy.mContext);
        builder.setMessage("蓝牙已经连接过，是否继续连接？");
        builder.setTitle("提示");
        builder.setOnKeyListener(CarApp.keylistener).setCancelable(false);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                boolean confirmation = device.setPairingConfirmation(true);
                for (int i = 0; i < 5; i++) {//发送上次，确保发送成功
                    if (confirmation) {
                        break;
                    }
                    confirmation = device.setPairingConfirmation(true);
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                boolean confirmation = device.setPairingConfirmation(false);
                for (int i = 0; i < 5; i++) {//发送上次，确保发送成功
                    if (confirmation) {
                        break;
                    }
                    confirmation = device.setPairingConfirmation(false);
                }
            }

        });
        dialog = builder.create();
        dialog.show();
    }
    /**
     * 发送取消和响应
     *
     * @param context
     * @param intent
     */
    private void sendReplyAndCancel(Context context, Intent intent) {
        // Log.e("hebing", "sink send reply...");
        final Intent reply = new Intent(
                "android.bluetooth.device.action.CONNECTION_ACCESS_REPLY");
        reply.setClassName("com.android.bluetooth", ".hfp.HeadsetService");
        reply.putExtra("android.bluetooth.device.extra.ALWAYS_ALLOWED", true);
        reply.putExtra(
                "android.bluetooth.device.extra.CONNECTION_ACCESS_RESULT", 1);
        reply.putExtra(BluetoothDevice.EXTRA_DEVICE,
                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
        context.sendBroadcast(reply,
                android.Manifest.permission.BLUETOOTH_ADMIN);

        // Log.e("hebing", "sink send cancel...");
        final Intent cancel = new Intent(
                "android.bluetooth.device.action.CONNECTION_ACCESS_CANCEL");
        cancel.putExtra(BluetoothDevice.EXTRA_DEVICE,
                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
        context.sendBroadcast(cancel);
    }

    @SuppressLint("NewApi")
    static void afterFoundBtHartModem(BluetoothDevice btDev) {
        ParcelUuid[] pus = btDev.getUuids();
        //此处逻辑有误，后面的代码没有执行过
        if (connectThread == null) {
            return;
        }
        //远程设备已经连接
        if (btDev.getBondState() == BluetoothDevice.BOND_BONDED) {
            connectThread = new ConnectThread(btDev);
            connectThread.start();
        }
    }


    private static class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server
                // code
                tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);//
                // 00001101-0000-1000-8000-00805F9B34FB
                // tmp = device.createRfcommSocket(1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.e(TAG, "run:==============连接");
            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
                Log.e(TAG, "run:==============socket连接上了");
            } catch (IOException connectException) {
                Log.e(TAG, "run: "+connectException.toString());
                connectException.printStackTrace();
                cancel();
                return;
            }
        }

        /**
         * Will cancel an in-progress connection, and close the socket
         */
        public void cancel() {
            try {
                connectThread = null;
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 取消连接线程
     */
    public static void cancel() {
        if (connectThread != null) {
            connectThread.cancel();
        }
    }

    // 取消用户输入
    public static boolean cancelPairingUserInput(BluetoothDevice device) throws Exception {
        Method createBondMethod = device.getClass().getMethod(
                "cancelPairingUserInput");
        // cancelBondProcess()
        Boolean returnValue = (Boolean) createBondMethod.invoke(device);
        return returnValue.booleanValue();
    }
}
