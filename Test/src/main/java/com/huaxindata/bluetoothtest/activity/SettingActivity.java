package com.huaxindata.bluetoothtest.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.huaxindata.bluetoothtest.R;
import com.huaxindata.bluetoothtest.util.Configuration;
import com.huaxindata.bluetoothtest.util.DBManager;

import java.io.IOException;

public class SettingActivity extends Activity {

    private TextView speak_text, speak_edit;
    private ImageView back;
    private TextView speaksettingbutton;
    private DBManager mgr;
    private CheckBox mSetBlFilterChBx;

    @SuppressLint("StringFormatMatches")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mgr = new DBManager(this);
        back = (ImageView) findViewById(R.id.setting_back);
        back.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                finish();
            }
        });
        /*mSetBlFilterChBx = (CheckBox) findViewById(R.id.setting_bl_filter_chbx);
        //设置蓝牙过滤器监听
        final SharedPreferences.Editor editor = CarApp.mPreferences.edit();
        //true代表只过滤蓝牙，false代表过滤vin
        mSetBlFilterChBx.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    editor.putBoolean("filter_bl", isChecked);
                } else {
                    editor.putBoolean("filter_bl", isChecked);
                }
                editor.commit();
            }
        });*/

    }
    public void historyData(View view) {
        Intent intent = new Intent(this, DataListActivity.class);
        startActivity(intent);
    }

    public void ipset(View view) {
        Intent intent = new Intent(this, SettingIPActivity.class);
        startActivity(intent);
    }

    public void settingvin(View view) {
        Intent intent = new Intent(this, SettingVINActivity.class);
        startActivity(intent);
    }

    public void cleandata(View view) {
        Builder builder = new Builder(this);
        builder.setMessage("确认清除检测数据吗（清除后不可恢复）？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mgr.deleteALL();
//                Util.execShellCmd("pm clear com.android.bluetooth");
            }
        });
        builder.setNegativeButton("取消", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    public void switchmode(View view) {
        Builder builder = new Builder(this);
        builder.setMessage("是否清除蓝牙数据？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
//                Util.execShellCmd("pm clear com.android.bluetooth");
//                Util.execShellCmd("rm /data/misc/bluedroid/*.*");
                try {
                    final Runtime runtime = Runtime.getRuntime();
                    final Process process =runtime.exec("pm clear com.android.bluetooth");
                    runtime.exec("rm /data/misc/bluedroid/*.*");
                    if (process != null) {
                        Toast.makeText(SettingActivity.this, "清除蓝牙数据成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SettingActivity.this, "清除蓝牙数据失败", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton("取消", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private int getTextType(String text) {
        if (text.contains("1")) {
            return 1;
        } else if (text.contains("2")) {
            return 2;
        } else if (text.contains("3")) {
            return 3;
        } else if (text.contains("4")) {
            return 4;
        }
        return 1;
    }

    private boolean setTextType(String text) {
        try {

            int temp = Integer.parseInt(text);
            if (temp == 1) {
                Configuration.SaveTextIndex(SettingActivity.this, "call1.bnf");
            } else if (temp == 2) {
                Configuration.SaveTextIndex(SettingActivity.this, "call2.bnf");
            } else if (temp == 3) {
                Configuration.SaveTextIndex(SettingActivity.this, "call3.bnf");
            } else if (temp == 4) {
                Configuration.SaveTextIndex(SettingActivity.this, "call4.bnf");
            } else {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
