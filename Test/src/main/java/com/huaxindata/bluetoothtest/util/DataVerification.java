package com.huaxindata.bluetoothtest.util;

import android.content.Context;

public class DataVerification {

	// public static final String MSG_WS_FIRST = "first[WS]";
	// public static final String MSG_WS_SECOND = "second[WS]";
	// public static final String MSG_WS_FIRST_OK = "first[WSOK]";
	// public static final String MSG_WS_SECOND_OK = "second[WSOK]";
	// public static final String MSG_WS_FIRST_NG = "first[WSNG]";
	// public static final String MSG_WS_SECOND_NG = "second[WSNG]";
	// public static final String MSG_TEST_LISTEN = "listen[END]";
	// public static final String MSG_TEST_LISTEN_REPLAY = "listenreplay[END]";
	/** 听音初始化完成 */
	// public static final String MSG_TEST_LISTEN_INIT = "listeninit[END]";
	/** 听音测试完成_OK */
	public static final String MSG_TESTIN_LISTEN_OK = "listen[OK]";
	/** 听音测试完成_NG */
	public static final String MSG_TESTIN_LISTEN_NG = "listen[NG]";
	// public static final String MSG_TEST_SPEACK = "speack[END]";
	/** 放音初始化完成 */
	// public static final String MSG_TEST_SPEACK_INIT = "speackinit[END]";
	public static final String MSG_TESTIN_SPEACK_OK = "speack[OK]";
	public static final String MSG_TESTIN_SPEACK_NG = "speack[NG]";
	// public static final String MSG_BACK_NOTING = "[NT]";
	// public static final String MSG_RESULT_SPEACK_OK = "resultspeack[OK]";
	// public static final String MSG_RESULT_SPEACK_NG = "resultspeack[NG]";
	// public static final String MSG_TEST_SPEACK_REPLAY = "speackreplay[END]";
	// public static final String MSG_TEST_OVER_OK = "over[OK]";
	// public static final String MSG_TEST_OVER_NG = "over[NG]";

	public static final String MSG_RESET = "reset[END]";
	public static final String MSG_XINTIAO = "xintiao[END]";
	public static final String MSG_XINTIAO_OK = "xintiao[OK]";

	public static final String MSG_CHECKIN_INDEX = "[INDEX]";

	/** 发送结果到服务器正常，发送给辅机用于存储 */
	// public static final String MSG_SENDTOSERVICE_OK = "send[OK]";
	/** 发送结果到服务器异常，发送给辅机用于存储 */
	// public static final String MSG_SENDTOSERVICE_NG = "send[NG]";

	// public static final String MSG_VIN_OK = "%d[OK]";

	public static boolean isValidService(String msg) {
		if (msg.trim().endsWith("[WS]")) {
			return true;
		} else if (msg.trim().endsWith("[END]")) {
			return true;
		} else if (msg.trim().endsWith("[OK]")) {
			return true;
		} else if (msg.trim().endsWith("[NG]")) {
			return true;
		} else if (msg.trim().endsWith("[INDEX]")) {
			return true;
		}
		return false;
	}

	/**
	 * 服务收到的数据
	 * 
	 * @param msg
	 * @param context
	 * @return
	 */
	public static int msgShowService(String msg, Context context) {
		if (msg.trim().equals(MSG_RESET)) {
			// return StateInfo.APP_RESET;
		} else if (msg.trim().endsWith(MSG_CHECKIN_INDEX)) {
			return StateInfo.CHECKIN_INDEX;
		}
		return 0;
	}

	public static String reDataService(String msg) {
		/** 服务器发送VIN码后回复 */
		if (msg.trim().endsWith("[END]")//
				&& msg.trim().startsWith("ST==")) {
			return msg.trim().replace("[END]", "[OK]");
		}
		return "[NG]";
	}

	// -------------------------------------

	/**
	 * 判断客户端发送请求后返回的数据的有效性（是否包含特殊标签）
	 * 
	 * @param msg
	 *            客户端发送请求后返回的数据
	 * @return
	 */
	public static boolean isValidClient(String msg) {
		if (msg.trim().endsWith("[WSOK]")) {
			return true;
		} else if (msg.trim().endsWith("[NG]")) {
			return true;
		} else if (msg.trim().endsWith("[END]")) {
			return true;
		} else if (msg.trim().endsWith("[OK]")) {
			return true;
		} else if (msg.trim().endsWith("[NT]")) {
			return true;
		} else if (msg.trim().endsWith("[BTOK]")) {
			return true;
		}
		return false;
	}

}
