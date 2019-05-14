package com.huaxindata.bluetoothtest.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.huaxindata.bluetoothtest.CarApp;
import com.huaxindata.bluetoothtest.R;

public class SettingVINActivity extends Activity {

    private ImageView back;
    private EditText vin;
    private EditText password;
    private EditText carStyle;
    private EditText carName;
    private EditText queryString;
    private TextView result;
    private Button button, queryBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settingvin);
        initview();
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
        vin = (EditText) findViewById(R.id.settingPort);
        password = (EditText) findViewById(R.id.settingvin_secret_et);
        carStyle = (EditText) findViewById(R.id.settingIp_socket_et);
        carName = (EditText) findViewById(R.id.settingIp_server_et);
        queryString = (EditText) findViewById(R.id.vin_name);
        result = (TextView) findViewById(R.id.chaxun_result);
        button = (Button) findViewById(R.id.setting_serverbutton);
        queryBtn = (Button) findViewById(R.id.chaxun_btn);
        back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                finish();
            }
        });
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                final String vinString = vin.getText().toString().equals("") ? null
                        : vin.getText().toString().trim();
                final String passwordString = password.getText().toString().equals("") ? null
                        : password.getText().toString().trim();
                final String carStyleString = carStyle.getText().toString().equals("") ? null
                        : carStyle.getText().toString().trim();
                final String carNameString = carName.getText().toString().equals("") ? null
                        : carName.getText().toString().trim();
                Editor editor = CarApp.mPreferences.edit();
                if (vinString != null && passwordString != null) {
                    editor.putString(vinString, vinString);
                    editor.putString(vinString + "password", passwordString);
                } else {
                    Toast.makeText(SettingVINActivity.this, "请输入VIN和密码后保存",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (carStyleString != null) {
                    editor.putString(vinString + "carStyleString", carStyleString);
                }
                if (carNameString != null) {
                    editor.putString(vinString + "carNameString", carNameString);
                }
                editor.commit();
                Toast.makeText(SettingVINActivity.this, "保存成功",
                        Toast.LENGTH_SHORT).show();
                vin.setText(null);
                password.setText(null);
                carStyle.setText(null);
                carName.setText(null);
            }
        });
        queryBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                String queryKeyword = queryString.getText().toString().equals("") ? null
                        : queryString.getText().toString().trim();
                SharedPreferences sp = CarApp.mPreferences;
                if (queryKeyword != null) {
                    if (sp.getString(queryKeyword, null) != null) {
                        String password = sp.getString(queryKeyword + "password", null);
                        String type = sp.getString(queryKeyword + "carStyleString", null);
                        String name = sp.getString(queryKeyword + "carNameString", null);
                        if (password != null) {
                            result.setText("VIN:" + queryKeyword +
                                    "\n密码:" + password +
                                    (TextUtils.isEmpty(type) ? null : ("\n型号:" + type)) +
                                    (TextUtils.isEmpty(name) ? null : ("\n中文名:" + name)));
                            result.setVisibility(View.VISIBLE);
                            queryString.setText(null);
                        } else {
                            result.setText("未查询到结果");
                            result.setVisibility(View.VISIBLE);
                        }

                    } else {
                        result.setText(null);
                        result.setVisibility(View.GONE);
                        Toast.makeText(SettingVINActivity.this, "未查到相关VIN信息",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SettingVINActivity.this, "请输入VIN后查询",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }

}
