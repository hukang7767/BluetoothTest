package com.huaxindata.bluetoothtest.util;

public interface StateInfo {

	public static final int DO_NOTING = -999;
	public static final int ERROR = 0;
	public static final int CHECK_START = 0x9001;
	public static final int START_ANIMATION = CHECK_START + 1;
	public static final int UPDATE_ANIMATION = CHECK_START + 2;
	public static final int LISTENER_OVER = CHECK_START + 4;
	public static final int SEND_RESULT = CHECK_START + 9;
	public static final int SEND_OK = CHECK_START + 7;
	public static final int SEND_NG = CHECK_START + 8;
	public static final int TEST_OVER = CHECK_START + 10;
	public static final int LISTENERING = CHECK_START + 11;
	public static final int RESET = CHECK_START + 13;
	public static final int WAIT = CHECK_START + 14;

	public static final int TOAST = CHECK_START + 15;

	public static final int RELEASE = CHECK_START + 16;
	public static final int START_ANOTHER_TEST=CHECK_START+17;

	public static final int TEST_GET_VIN = 1;// 获得VIN

	public static final int CHECKIN_INDEX = 8;

	// public static final int CHAOSHI = 99;
}
