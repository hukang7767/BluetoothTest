/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huaxin.emmp_ard.scan.decoding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.example.carplate.CarPlateDetection;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.huaxin.emmp_ard.scan.camera.CameraManager;
import com.huaxin.emmp_ard.scan.camera.PlanarYUVLuminanceSource;
import com.huaxin.emmp_ard.scancapture.R;
import com.huaxin.emmp_ard.scancapture.ScanCaptureActivity;

public final class DecodeHandler extends Handler {

	private static final String TAG = DecodeHandler.class.getSimpleName();

	private final ScanCaptureActivity activity;
	private final MultiFormatReader multiFormatReader;

	DecodeHandler(ScanCaptureActivity activity,
			Hashtable<DecodeHintType, Object> hints) {
		multiFormatReader = new MultiFormatReader();
		multiFormatReader.setHints(hints);
		this.activity = activity;
	}

	@Override
	public void handleMessage(Message message) {
		if (message.what == R.id.decode) {
			// Log.d(TAG, "Got decode message");
			decode((byte[]) message.obj, message.arg1, message.arg2);
		} else if (message.what == R.id.quit) {
			Looper.myLooper().quit();
		}
	}

	/**
	 * Decode the data within the viewfinder rectangle, and time how long it
	 * took. For efficiency, reuse the same reader objects from one decode to
	 * the next.
	 * 
	 * @param data
	 *            The YUV preview frame.
	 * @param width
	 *            The width of the preview frame.
	 * @param height
	 *            The height of the preview frame.
	 */
	private void decode(byte[] data, int width, int height) {
		long start = System.currentTimeMillis();
		if (CarPlateDetection.TEST) {
			// 08-04 11:36:59.208: I/System.out(22802): get:720000&480000
			// System.out.println("get:" + data.length + "&" + (width *
			// height));
			if (data != null) {
				// byte[] rotated = new byte[data.length];
				// for (int y = 0; y < width; y++) {
				// for (int x = 0; x < height; x++) {
				// rotated[x * width + width - y - 1] = data[x + y
				// * height];
				// }
				// }
				// int i, j, wh, k;
				// k = wh = width * height;
				// for (j = height / 2; j > 0; j--)
				// for (i = 0; i < width / 2; i++) {
				// rotated[k++] = data[wh + height / 2 * i + j - 1];
				// }
				// wh = width * height + height / 2 * width / 2;
				// for (j = height / 2; j > 0; j--)
				// for (i = 0; i < width / 2; i++) {
				// rotated[k++] = data[wh + height / 2 * i + j - 1];
				// }
				// int begin = width * height;
				// int m = 0, n = 0;
				// for (int y = 0; y < width / 2; y++) {
				// for (int x = 0; x < height; x++) {
				// rotated[begin + x * width + width - y - 1] = back_[begin
				// + x + y * height];
				// }
				// }
				// for (int x = 0; x < height; x += 2) {
				// m = 0;
				// for (int y = 0; y < width / 2; y++) {
				// rotated[begin + n + width - y - 2] = back_[begin + m
				// + x];
				// rotated[begin + n + width - y - 1] = back_[begin + m
				// + x + 1];
				// // begin += 2;
				// m += height;
				// }
				// n += width;
				// }
				// PlanarYUVLuminanceSource ps = CameraManager.get()
				// .buildLuminanceSource(rotated, width, height);
				// String get = saveBitmap(ps.getMatrix(), ps.getWidth(),
				// ps.getHeight());
				String get = saveBitmap(data, width, height);
				if (get != null) {
					long end = System.currentTimeMillis();
					Log.d(TAG, "Found car (" + (end - start) + " ms):\n" + get);
					Message message = Message.obtain(activity.getHandler(),
							R.id.decode_succeeded, get);
					message.sendToTarget();
				} else {
					Message message = Message.obtain(activity.getHandler(),
							R.id.decode_failed);
					message.sendToTarget();
				}
			}
		} else {
			Result rawResult = null;
			//
			byte[] rotatedData = new byte[data.length];
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++)
					rotatedData[x * height + height - y - 1] = data[x + y
							* width];
			}
			int tmp = width; // Here we are swapping, that's the difference to
								// #11
			width = height;
			height = tmp;
			// data = rotatedData;
			//
			PlanarYUVLuminanceSource source = CameraManager.get()
					.buildLuminanceSource(rotatedData, width, height);

			// final byte[] get_extra = source.getMatrix();

			BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
			try {
				rawResult = multiFormatReader.decodeWithState(bitmap);
			} catch (ReaderException re) {
				// continue
			} finally {
				multiFormatReader.reset();
			}
			if (rawResult != null) {
				if (!CarPlateDetection.TEST) {
					saveBitmap(data, height, width);
				}
				long end = System.currentTimeMillis();
				Log.d(TAG, "Found barcode (" + (end - start) + " ms):\n"
						+ rawResult.toString());
				Message message = Message.obtain(activity.getHandler(),
						R.id.decode_succeeded, rawResult);
				Bundle bundle = new Bundle();
				bundle.putParcelable(DecodeThread.BARCODE_BITMAP,
						source.renderCroppedGreyscaleBitmap());
				message.setData(bundle);
				// Log.d(TAG, "Sending decode succeeded message...");
				message.sendToTarget();
			} else {
				Message message = Message.obtain(activity.getHandler(),
						R.id.decode_failed);
				message.sendToTarget();
			}
		}
	}

	// public static byte[] YUV420SP2YUV420(byte[] yuv420sp, int width, int
	// height) {
	// if (yuv420sp == null)
	// return null;
	// byte[] yuv420 = new byte[yuv420sp.length];
	// int framesize = width * height;
	// int i = 0, j = 0;
	// // copy y
	// for (i = 0; i < framesize; i++) {
	// yuv420[i] = yuv420sp[i];
	// }
	// i = 0;
	// for (j = 0; j < framesize / 2; j += 2) {
	// yuv420[i + framesize * 5 / 4] = yuv420sp[j + framesize];
	// i++;
	// }
	// i = 0;
	// for (j = 1; j < framesize / 2; j += 2) {
	// yuv420[i + framesize] = yuv420sp[j + framesize];
	// i++;
	// }
	// return rotateYUV240SP(yuv420, width, height);
	// }
	//
	// public static byte[] rotateYUV240SP(byte[] src, int width, int height) {
	// byte[] des = new byte[src.length];
	// int wh = width * height;
	// // 旋转Y
	// int k = 0;
	// for (int i = 0; i < width; i++) {
	// for (int j = 0; j < height; j++) {
	// des[k] = src[width * j + i];
	// k++;
	// }
	// }
	// for (int i = 0; i < width; i += 2) {
	// for (int j = 0; j < height / 2; j++) {
	// des[k] = src[wh + width * j + i];
	// des[k + 1] = src[wh + width * j + i + 1];
	// k += 2;
	// }
	// }
	// return des;
	// }

	public static String saveBitmap(byte[] data_, int width, int height) {
		final YuvImage yuv = new YuvImage(data_, ImageFormat.NV21, width,
				height, null);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		yuv.compressToJpeg(new Rect(0, 0, width, height), 100, out);

		byte[] bytes = out.toByteArray();
		Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
		Matrix matrix = new Matrix();
		matrix.postRotate(90);
		// // 创建新的图片
		bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), matrix, true);

		// saveBitmap(bitmap);

		bitmap = crop(bitmap, CameraManager.get().getFramingRectInPreview());
		// bitmap = ImgPretreatment.doPretreatment(bitmap);
		String get = null;
		if (bitmap != null) {
			System.out.println("1 get: " + bitmap);
			if (bitmap != null) {
				get = getCarPlate(bitmap);
				bitmap.recycle();
			}
		} else {
			System.out.println("failed to get: " + bitmap);
		}
		return get;
	}

	static int i = 0;

	public static String getCarPlate(Bitmap bmp) {
		File old = new File(Environment.getExternalStorageDirectory().getPath()
				+ "/bmp" + (i) + ".jpg");
		File f = new File(Environment.getExternalStorageDirectory().getPath()
				+ "/bmp" + (++i) + ".jpg");
		System.out.println("car save to : " + f.getPath());
		if (old.exists()) {
			old.delete();
		}
		try {
			FileOutputStream out = new FileOutputStream(f);
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.flush();
			out.close();
			if (CarPlateDetection.TEST) {
				return CarPlateDetection.ImageProc(f.getPath());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void saveBitmap(Bitmap bmp) {
		File f = new File(Environment.getExternalStorageDirectory().getPath()
				+ "/bmp_all2" + ".jpg");
		System.out.println("car save to : " + f.getPath());
		if (f.exists()) {
			f.delete();
		}
		try {
			FileOutputStream out = new FileOutputStream(f);
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static int[] getRow(int[] yuvData, int dataWidth, int y, Rect rect) {
		int[] row = new int[rect.width()];
		int offset = (y + rect.top) * dataWidth + rect.left;
		System.arraycopy(yuvData, offset, row, 0, rect.width());
		return row;
	}

	public static Bitmap crop(Bitmap bmp, Rect rect) {
		int h = bmp.getHeight();
		int w = bmp.getWidth();
		int[] pixels = new int[w * h]; // 通过位图的大小创建像素点数组
		bmp.getPixels(pixels, 0, w, 0, 0, w, h);

		int[] gets = new int[rect.width() * rect.height()];
		for (int i = 0; i < rect.height(); i++) {
			int[] bb = getRow(pixels, w, i, rect);
			System.arraycopy(bb, 0, gets, i * rect.width(), rect.width());
		}
		// // rotated
		// int[] rotatedData = new int[pixels.length];
		// for (int y = 0; y < height; y++) {
		// for (int x = 0; x < width; x++)
		// rotatedData[x * height + height - y - 1] = pixels[x + y * width];
		// }
		// int tmp = width; // Here we are swapping, that's the difference
		// // to #11
		// width = height;
		// height = tmp;
		//
		Bitmap bm = Bitmap.createBitmap(rect.width(), rect.height(),
				Config.RGB_565);
		bm.setPixels(gets, 0, rect.width(), 0, 0, rect.width(), rect.height());
		// saveBitmap(bm);
		bmp.recycle();
		return bm;
	}
}
