package com.huaxindata.bluetoothtest.entity;

/**
 * Created by Administrator on 2017/6/8.
 */

public class NetConfig {
    private static String IP;
    private static int PORT = 2223;
    private static int MAX_TEST = 45 * 1000;

    private static boolean IS_NETWORK_OK = false;

    public static int getMaxTest() {
        return MAX_TEST;
    }

    public static void setMaxTest(int maxTest) {
        MAX_TEST = maxTest;
    }

    public static String getIP() {
        return IP;
    }

    public static void setIP(String IP) {
        NetConfig.IP = IP;
    }

    public static int getPORT() {
        return PORT;
    }

    public static void setPORT(int PORT) {
        NetConfig.PORT = PORT;
    }

    public static boolean isNetworkOk() {
        return IS_NETWORK_OK;
    }

    public static void setIsNetworkOk(boolean isNetworkOk) {
        IS_NETWORK_OK = isNetworkOk;
    }
}
