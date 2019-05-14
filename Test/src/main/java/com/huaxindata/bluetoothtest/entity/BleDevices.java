package com.huaxindata.bluetoothtest.entity;

/**
 * Created by sunanoe on 2017/7/7.
 * 一个用于记录已经同意连接的车的mac地址
 */

public class BleDevices {
    private static String CURRENT_PAIRED_MAC="";
    public static void setCurrent_Paired_mac(String mac){
        synchronized (CURRENT_PAIRED_MAC) {
            CURRENT_PAIRED_MAC=mac;
        }
    }
    public static String getCurrentPairedMac(){
        return CURRENT_PAIRED_MAC;
    }
}
