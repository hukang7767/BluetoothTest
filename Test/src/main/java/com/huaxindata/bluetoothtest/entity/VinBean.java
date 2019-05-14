package com.huaxindata.bluetoothtest.entity;

/**
 * Created by Administrator on 2017/5/20.
 */

public class VinBean {
    private static String lastVin=null;
    private static String Vin=null;

    public static synchronized String getLastVin() {
        return lastVin;
    }

    public static synchronized String getVin() {
        return Vin;
    }

    public static synchronized void setLastVin(String lastVin) {
        VinBean.lastVin = lastVin;
    }

    public static synchronized void setVin(String vin) {
        VinBean.Vin = vin;
    }
}
