package com.huaxindata.bluetoothtest.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huaxindata.bluetoothtest.CarApp;
import com.huaxindata.bluetoothtest.R;
import com.huaxindata.bluetoothtest.entity.NetConfig;
import com.huaxindata.bluetoothtest.util.Configuration;
import com.huaxindata.bluetoothtest.util.Util;

public class SettingIPActivity extends Activity {

	private ImageView back;
	private TextView socketName;
	private EditText socketPort;
	private EditText socketEt;
	private EditText edt_max_max;
	private EditText edt_autoLine_time;
//	private TextView serverIP;
//	private EditText serverEt;
private EditText serverEt1;
	private EditText serverEt2;
	private EditText serverEt3;
	private EditText serverEt4;
	private EditText vol;
	private Button button;
	private RelativeLayout layout;
	private TextView loaclIp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settingip);
		initview();
		socketName.setText(getString(R.string.ip_secund));
		if (NetConfig.getIP() != null) {// IP为空
			String iptemp = NetConfig.getIP();
			String[] temp = iptemp.split("\\.");
			serverEt1.setText(temp[temp.length - 4]);
			serverEt2.setText(temp[temp.length - 3]);
			serverEt3.setText(temp[temp.length - 2]);
			serverEt4.setText(temp[temp.length - 1]);
			// }
		} else {
			if ("".equals(Util.NetworkIp(this))) {
				Builder builder = new Builder(this);
				builder.setMessage("网络未连接，请检查");
				builder.setTitle("提示");
				builder.setOnKeyListener(keylistener);
				builder.setPositiveButton("确认",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								finish();
								Intent intent = null;
								// 判断手机系统的版本 即API大于10 就是3.0或以上版本
								if (android.os.Build.VERSION.SDK_INT > 10) {
									intent = new Intent(
											android.provider.Settings.ACTION_WIRELESS_SETTINGS);
								} else {
									intent = new Intent();
									ComponentName component = new ComponentName(
											"com.android.settings",
											"com.android.settings.WirelessSettings");
									intent.setComponent(component);
									intent.setAction("android.intent.action.VIEW");
								}
								startActivity(intent);
							}
						});
				builder.create().show();
			} else {
				String ipString = Util.NetworkIp(this);
				String[] splitIp = ipString.split("\\.");
				serverEt1.setText(splitIp[0]);
				serverEt2.setText(splitIp[1]);
				serverEt3.setText(splitIp[2]);
			}
		}
	}

	OnKeyListener keylistener = new OnKeyListener() {
		public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
				return true;
			} else {
				return false;
			}
		}
	};

	@SuppressLint("StringFormatMatches")
	private void initview() {
		loaclIp = (TextView) findViewById(R.id.setting_loaclip);
		socketPort = (EditText) findViewById(R.id.settingPort);
		loaclIp.setText(String.format(getString(R.string.localip), Util.NetworkSetting(this), "s"));

		socketName = (TextView) findViewById(R.id.settingIp_socket_tv);
		// socketIP = (TextView) findViewById(R.id.settingIp_socket);
		socketEt = (EditText) findViewById(R.id.settingIp_socket_et);
		serverEt1 = (EditText) findViewById(R.id.settingIp_server_et1);
		serverEt2 = (EditText) findViewById(R.id.settingIp_server_et2);
		serverEt3 = (EditText) findViewById(R.id.settingIp_server_et3);
		serverEt4 = (EditText) findViewById(R.id.settingIp_server_et4);
		edt_max_max = (EditText) findViewById(R.id.edt_max_max);
		edt_autoLine_time = (EditText) findViewById(R.id.edt_autoLine_time);
		vol = (EditText) findViewById(R.id.setting_vol);
		socketEt.setHint(""+ NetConfig.getMaxTest()/1000);
		vol.setHint(""+Configuration.getVol(this));
		edt_max_max.setHint(""+Configuration.getMaxMAc(this));
		edt_autoLine_time.setHint(""+Configuration.getAutoLine(this));
		socketPort.setHint("" + NetConfig.getPORT());

		button = (Button) findViewById(R.id.setting_serverbutton);
		back = (ImageView) findViewById(R.id.back);
		layout = (RelativeLayout) findViewById(R.id.settingIp_remote_layout);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				final String newIp1 =serverEt1.getText().toString().trim();
				final String newIp2 =serverEt2.getText().toString().trim();
				final String newIp3 =serverEt3.getText().toString().trim();
				final String newIp4 =serverEt4.getText().toString().trim();

				final String newIp=generateIp(newIp1,newIp2,newIp3,newIp4);
				if (newIp == null) {
					Toast.makeText(SettingIPActivity.this,"ip设置不合法，请重新设置",Toast.LENGTH_SHORT).show();
					return;
				}
				final int newPort = socketPort.getText().toString().equals("") ? -1
						: Integer.parseInt(socketPort.getText().toString());
				final int newTimeout = socketEt.getText().toString().equals("") ? -1
						: Integer.parseInt(socketEt.getText().toString());
				Editor editor = CarApp.mPreferences.edit();
				if (newIp != null) {
					NetConfig.setIP(newIp);
					editor.putString("IP", newIp);
				}
				if (newPort > 0) {
					NetConfig.setPORT(newPort);
					editor.putInt("PORT", newPort);
				}
				if (newTimeout > 0) {
					NetConfig.setMaxTest(1000 * newTimeout);
					editor.putInt("MAX_TIME",1000*newTimeout);
				}
				final String volStr = vol.getText().toString().trim();
				String maxMacStr = edt_max_max.getText().toString().trim();
				String autoLineStr = edt_autoLine_time.getText().toString().trim();
				if (!TextUtils.isEmpty(volStr)) {
					int volInt=0;
					try {
						volInt = Integer.parseInt(volStr);
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}finally {
						if (volInt > 0) {
							Configuration.setVol(getApplicationContext(),volInt );
						}
					}
				}
				if (!TextUtils.isEmpty(maxMacStr)) {
					int maxMac=0;
					try {
						maxMac = Integer.parseInt(maxMacStr);
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}finally {
						if (maxMac > 0) {
							Configuration.setMaxMac(getApplicationContext(),maxMac);
						}
					}
				}
				if (!TextUtils.isEmpty(autoLineStr)) {
					int autoLine=0;
					try {
						autoLine = Integer.parseInt(autoLineStr);
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}finally {
						if (autoLine > 0) {
							Configuration.setAutoLine(getApplicationContext(),autoLine);
						}
					}
				}
				editor.commit();
				Log.e("SettingIpActivity", "onClick:====ip已经改变ip:"+newIp+"==超时："+newTimeout);
				Toast.makeText(SettingIPActivity.this, "保存成功，正在重新创建连接",
						Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(SettingIPActivity.this, MainHomeActivtiy.class);
				intent.putExtra("resetNet", true);
				startActivity(intent);
				finish();
			}
		});
	}

	private String generateIp(String newIp1, String newIp2, String newIp3, String newIp4) {
		String string=null;
		if (checkIp(newIp1) && checkIp(newIp2) && checkIp(newIp3) && checkIp(newIp4)) {
			string=newIp1+"."+newIp2+"."+newIp3+"."+newIp4;
		}
		return string;
	}
	private boolean checkIp(String ip){
		if (ip == null||"".equals(ip)) {
			return false;
		}
		int i = Integer.parseInt(ip);
		if (i<=255&&i>=0)
			return true;
		return false;
	}
}
