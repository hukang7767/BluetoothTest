package com.example.carplate;

import java.io.UnsupportedEncodingException;

public class CarPlateDetection {

	public static boolean load = false;
	public static boolean TEST = false;
	public static String sdpath;
	private static final int[] indexs = new int[] { 1, 2, 4, 5, 6, 7, 8 };

	public static String ImageProc(String path) {
		if (!load) {
			load = true;
			load();
		}
		String svmpath = sdpath + "/svm.xml";
		String annpath = sdpath + "/ann.xml";
		String imgpath = path;// + "/test" + 2 + ".jpg";
		byte[] resultByte = CarPlateDetection.ImageProc(sdpath, imgpath,
				svmpath, annpath);
		String result = null;
		try {
			result = new String(resultByte, "utf-8");
			System.out.println(result);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if (result != null && result.length() > 5) {
			return result;
		} else {
			return null;
		}
	}

	static void load() {
		System.loadLibrary("gnustl_shared"); // added
		System.loadLibrary("opencv_java");
		System.loadLibrary("imageproc");
	}

	public static native byte[] ImageProc(String sdpath, String imgpath,
			String svmpath, String annpath);
}
