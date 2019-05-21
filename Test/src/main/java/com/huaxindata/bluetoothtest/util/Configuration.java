package com.huaxindata.bluetoothtest.util;

import android.content.Context;
import android.content.SharedPreferences;

public class Configuration {

	public static final int CHECK_TRUE = 1;
	public static final int CHECK_FLASE = 0;

	public static final int UPLOAD_TURE = 1;
	public static final int UPLOAD_FLASE = -1;

	//private static final String TEXT_CHOOSE = "choose";
	private static final String TEXT_SAVE = "save";
	//private static final String TEXT_IP = "ip";
	//private static final String TEXT_PORT = "port";

	private static final String PC_IP = "pcip";
	private static final String PC_PORT = "pcport";

	private static final String TextIndex = "textindex";

	public static boolean loadIsSave(Context context) {
		SharedPreferences prefs = context.getSharedPreferences("setting", 0);
		return prefs.getBoolean(TEXT_SAVE, false);
	}

	public static void saveIsSave(Context context, boolean choose) {
		SharedPreferences.Editor prefs = context.getSharedPreferences(
				"setting", 0).edit();
		prefs.putBoolean(TEXT_SAVE, choose);
		prefs.commit();
	}

//	public static boolean loadChooseIsMain(Context context) {
//		SharedPreferences prefs = context.getSharedPreferences("setting", 0);
//		return prefs.getBoolean(TEXT_CHOOSE, true);
//	}
//
//	public static void saveChooseIsMain(Context context, boolean choose) {
//		SharedPreferences.Editor prefs = context.getSharedPreferences(
//				"setting", 0).edit();
//		prefs.putBoolean(TEXT_CHOOSE, choose);
//		prefs.commit();
//	}

//	public static void saveIpAndPort(Context context, String ip, int prot) {
//		SharedPreferences.Editor prefs = context.getSharedPreferences(
//				"setting", 0).edit();
//		prefs.putString(TEXT_IP, ip);
//		prefs.putInt(TEXT_PORT, prot);
//		prefs.commit();
//	}
//
//	public static String loadIP(Context context) {
//		SharedPreferences prefs = context.getSharedPreferences("setting", 0);
//		return prefs.getString(TEXT_IP, "");
//	}
//
//	public static int loadProt(Context context) {
//		SharedPreferences prefs = context.getSharedPreferences("setting", 0);
//		return prefs.getInt(TEXT_PORT, 0);
//	}

	public static void savePcIpAndPcPort(Context context, String ip, int prot) {
		SharedPreferences.Editor prefs = context.getSharedPreferences(
				"setting", 0).edit();
		prefs.putString(PC_IP, ip);
		prefs.putInt(PC_PORT, prot);
		prefs.commit();
	}
	public static void setLogOpen(Context c,boolean isOpen){
		SharedPreferences.Editor prefs = c.getSharedPreferences(
				"setting", 0).edit();
		prefs.putBoolean("log", isOpen);
		prefs.commit();
	}
	public static boolean getLogOpen(Context c){
		SharedPreferences prefs = c.getSharedPreferences("setting", 0);
		return prefs.getBoolean("log",false);
	}
	public static void setVol(Context c,int val){
		SharedPreferences.Editor prefs = c.getSharedPreferences(
				"setting", 0).edit();
		prefs.putInt("vol", val);
		prefs.commit();
	}
	public static int getVol(Context c){
		SharedPreferences prefs = c.getSharedPreferences("setting", 0);
		return prefs.getInt("vol",10);
	}
	public static void setMaxMac(Context c,int max){
		SharedPreferences.Editor prefs = c.getSharedPreferences(
				"setting", 0).edit();
		prefs.putInt("settingMac", max);
		prefs.commit();
	}
	public static int getMaxMAc(Context c){
		SharedPreferences prefs = c.getSharedPreferences("setting", 0);
		return prefs.getInt("settingMac",100);
	}
//	public static String loadPcIP(Context context) {
//		SharedPreferences prefs = context.getSharedPreferences("setting", 0);
//		return prefs.getString(PC_IP, "");
//	}

	public static int loadPcProt(Context context) {
		SharedPreferences prefs = context.getSharedPreferences("setting", 0);
		return prefs.getInt(PC_PORT, 0);
	}

	public static String loadTextIndex(Context context) {
		SharedPreferences prefs = context.getSharedPreferences("setting", 0);
		return prefs.getString(TextIndex, "call1.bnf");
	}

	public static void SaveTextIndex(Context context, String name) {
		SharedPreferences.Editor prefs = context.getSharedPreferences(
				"setting", 0).edit();
		prefs.putString(TextIndex, name);
		prefs.commit();
	}
}
