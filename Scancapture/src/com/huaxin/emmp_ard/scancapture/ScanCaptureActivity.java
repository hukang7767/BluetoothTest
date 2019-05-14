package com.huaxin.emmp_ard.scancapture;

import java.io.IOException;
import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.example.carplate.CarPlateDetection;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.huaxin.emmp_ard.scan.camera.CameraManager;
import com.huaxin.emmp_ard.scan.camera.FlashlightManager;
import com.huaxin.emmp_ard.scan.decoding.CaptureActivityHandler;
import com.huaxin.emmp_ard.scan.decoding.InactivityTimer;
import com.huaxin.emmp_ard.scan.view.ViewfinderView;

public class ScanCaptureActivity extends Activity implements Callback {

	private String Tag = "ScanCaptureActivity";
	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	private TextView txtResult,tishiText;
	private InactivityTimer inactivityTimer;
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 0.10f;
	private boolean vibrate;
	// ----@phj-------
	private Button openLigth;
	private Boolean LigthOpen = false;

	// ------------------

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scan_main);
		// ��ʼ�� CameraManager
		CameraManager.init(getApplication());
		CarPlateDetection.TEST = false;
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		txtResult = (TextView) findViewById(R.id.txtResult);
		tishiText = (TextView) findViewById(R.id.tishi_text);
		if(getIntent().hasExtra("index")){
			if(1==getIntent().getIntExtra("index", 3)){
				tishiText.setText("请扫描条形码");
			}else if(2==getIntent().getIntExtra("index", 3)){
				CarPlateDetection.TEST = true;
				CarPlateDetection.sdpath = getIntent().getStringExtra("path");
				tishiText.setText("请扫描车辆二维码");
				
			}else if(3==getIntent().getIntExtra("index", 3)){
				tishiText.setText("请扫描条形码或二维码");
			}
		}else {
			tishiText.setText("请扫描条形码或二维码");
		}
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
		
		// ----@phj-------
		openLigth = (Button) findViewById(R.id.button_ligth);
		openLigth.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (LigthOpen) {
					FlashlightManager.disableFlashlight();
					LigthOpen = false;
					openLigth.setText("打开闪光灯");
				} else {
					FlashlightManager.enableFlashlight();
					LigthOpen = true;
					openLigth.setText("关闭闪光灯");
				}

			}
		});
		// -------------
	}

	@Override
	protected void onResume() {
		super.onResume();
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		decodeFormats = null;
		characterSet = null;

		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
		// initBeepSound();
		vibrate = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		if (handler == null) {
			handler = new CaptureActivityHandler(this, decodeFormats,
					characterSet);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;

	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();

	}

	public void handleDecode(Result obj, Bitmap barcode) {
		inactivityTimer.onActivity();
		viewfinderView.drawResultBitmap(barcode);
		playBeepSoundAndVibrate();
		txtResult.setText(obj.getBarcodeFormat().toString() + ":"
				+ obj.getText());

		Log.i(Tag, "条码为--->" + obj.getText() + "type:" + obj.getBarcodeFormat());
		Intent intent = new Intent();
		intent.putExtra("code", obj.getText());
		intent.putExtra("type", obj.getBarcodeFormat().toString());
		setResult(RESULT_OK, intent);
		finish();
	}

	public void handleDecode(String obj, Bitmap barcode) {
		inactivityTimer.onActivity();
		viewfinderView.drawResultBitmap(barcode);
		playBeepSoundAndVibrate();
		txtResult.setText(obj + ":" + obj);

		Log.i(Tag, "条码为--->" + obj + "type:" + obj);
		Intent intent = new Intent();
		intent.putExtra("code", obj);
		intent.putExtra("type", obj);
		setResult(RESULT_OK, intent);
		finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 手机返回按钮
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			inactivityTimer.onActivity();
			Intent intent = new Intent();
			intent.putExtra("code", "");
			setResult(RESULT_OK, intent);
			CameraManager.get().closeDriver();
			finish();
			onDestroy();
			return false;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	// private void initBeepSound() {
	// if (playBeep && mediaPlayer == null) {
	// // The volume on STREAM_SYSTEM is not adjustable, and users found it
	// // too loud,
	// // so we now play on the music stream.
	// setVolumeControlStream(AudioManager.STREAM_MUSIC);
	// mediaPlayer = new MediaPlayer();
	// mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
	// mediaPlayer.setOnCompletionListener(beepListener);
	//
	// AssetFileDescriptor file = getResources().openRawResourceFd(
	// R.raw.beep);
	// try {
	// mediaPlayer.setDataSource(file.getFileDescriptor(),
	// file.getStartOffset(), file.getLength());
	// file.close();
	// mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
	// mediaPlayer.prepare();
	// } catch (IOException e) {
	// mediaPlayer = null;
	// }
	// }
	// }

	private static final long VIBRATE_DURATION = 200L;

	private void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};

}