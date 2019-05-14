package com.huaxindata.bluetoothtest.ifly;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

import com.iflytek.cloud.SpeechUtility;

import java.io.File;


/**
 * 弹出提示框，下载服务组件
 */
public class ApkInstaller {
	private Activity mActivity ;
	
	public ApkInstaller(Activity activity) {
		mActivity = activity;
	}

	public void install(){
		Builder builder = new Builder(mActivity);
		builder.setMessage("检测到您未安装语记！\n是否前往下载语记？");
		builder.setTitle("下载提示");
		builder.setPositiveButton("确认前往", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				String url = SpeechUtility.getUtility().getComponentUrl();
				String assetsApk="SpeechService.apk";
//				processInstall(mActivity, url,assetsApk);
				processInstallFromAssets(mActivity, url, assetsApk);
			}
		});
		builder.setNegativeButton("残忍拒绝", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
		return;
	}
	private boolean processInstallFromAssets(Context context ,String url,String assetsApk){
		Intent intent = new Intent(Intent.ACTION_VIEW);
		Uri mUri;
		if (Build.VERSION.SDK_INT >= 24) {
			mUri = FileProvider.getUriForFile(context, "com.xxx.fileprovider", new File(assetsApk));
		} else {
			mUri = Uri.fromFile(new File(assetsApk));
		}
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(mUri,
				"application/vnd.android.package-archive");
		context.startActivity(intent);
		return true;
	}
	/**
	 * 如果服务组件没有安装打开语音服务组件下载页面，进行下载后安装。
	 */
	private boolean processInstall(Context context ,String url,String assetsApk){
		//直接下载方式
		Uri uri = Uri.parse(url);
		Intent it = new Intent(Intent.ACTION_VIEW, uri);
		context.startActivity(it);
		return true;		
	}
}
