package com.huaxindata.bluetoothtest.entity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.huaxindata.bluetoothtest.ListenSoundListener;
import com.huaxindata.bluetoothtest.R;
import com.huaxindata.bluetoothtest.ifly.ApkInstaller;
import com.huaxindata.bluetoothtest.ifly.FucUtil;
import com.huaxindata.bluetoothtest.ifly.JsonParser;
import com.huaxindata.bluetoothtest.util.Configuration;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by Administrator on 2017/6/23.
 */

public class SpeechListenner {
    private static String TAG = SpeechListenner.class.getSimpleName();
    // 语音听写对象
    private SpeechRecognizer mRecognizer;
    // 语音听写UI
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();

    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_LOCAL;
    // 语记安装助手类
    ApkInstaller mInstaller;

    private Context mContext;
    private Toast mToast;
    ListenSoundListener mSoundListener;//回调到主界面的listener
    private static int sVol;

    public SpeechListenner(Context context,ListenSoundListener listenSoundListener) {
        this.mContext=context;
        this.mSoundListener=listenSoundListener;
        init();
    }

    public void init(){
        mToast = Toast.makeText(mContext, "", Toast.LENGTH_SHORT);
        mInstaller = new ApkInstaller((Activity) mContext);
        // 初始化识别无UI识别对象
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mRecognizer = SpeechRecognizer.createRecognizer(mContext, mInitListener);
        mEngineType = SpeechConstant.TYPE_LOCAL;
        check();
    }

    private int getVolume() {
        int vol= Configuration.getVol(mContext);
        Log.e(TAG, "init: =====================设置的检测音量："+ vol);
        return vol;
    }

    /**
     * 选择本地听写,判断是否安装语记,未安装则跳转到提示安装页面
     */
    private void check() {
        if (!SpeechUtility.getUtility().checkServiceInstalled()) {
            mInstaller.install();
        } else {
            String result = FucUtil.checkLocalResource();
            if (!TextUtils.isEmpty(result)) {
                showTip(result);
            }
        }
    }

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败，错误码：" + code);
                mSoundListener.init(false);
                Log.e(TAG, "onInit: ===========================初始化语音识别失败:"+code);
            } else {
                mSoundListener.init(true);
                Log.e(TAG, "onInit: ===================语音识别初始化成功:"+code);
            }
        }
    };
    int ret = 0; // 函数调用返回值
    public void startListen(){
        check();//检查是否安装语记
        mIatResults.clear();
        // 设置参数
        setParam();
        // 不显示听写对话框
        ret = mRecognizer.startListening(mRecognizerListener);
        if (ret != ErrorCode.SUCCESS) {
            showTip("听写失败,错误码：" + ret);
        } else {
            showTip(mContext.getString(R.string.text_begin));
        }
    }

    /**
     * 听写监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            Log.e(TAG, "onBeginOfSpeech: ==============可以开始说话了");
            sVol =getVolume();
            mIatResults.clear();
        }

        @Override
        public void onError(SpeechError error) {
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            // 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
            mSoundListener.listenMsg(null,true);
            Log.e(TAG, "onError: ======================识别出错，错误码："+error.getErrorCode());
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            Log.e(TAG, "onEndOfSpeech: ===============说话完毕，不再接受语音输入");
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            String text = JsonParser.parseIatResult(results.getResultString());
            String sn = null;
            // 读取json结果中的sn字段
            try {
                JSONObject resultJson = new JSONObject(results.getResultString());
                sn = resultJson.optString("sn");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mIatResults.put(sn, text);
            StringBuffer resultBuffer = new StringBuffer();
            for (String key : mIatResults.keySet()) {
                resultBuffer.append(mIatResults.get(key));
            }
            mSoundListener.listenMsg(resultBuffer.toString(),true);
            Log.e(TAG, "onResult: =================识别结果："+resultBuffer.toString()+"===isLast"+isLast);
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            if (volume>= sVol) {
                Log.e(TAG, "音量识别达到设定标准====================" + volume);
                mSoundListener.listenMsg("" + volume,false);
            }
//            Log.e(TAG, "onVolumeChanged:=====设置的检测音量："+ sVol +"\t检测到的音量："+volume);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };
    /**
     * 参数设置
     *
     * @return
     */
    public void setParam() {
        // 清空参数
        mRecognizer.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎
        mRecognizer.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        mRecognizer.setParameter( SpeechConstant.ENGINE_MODE, SpeechConstant.MODE_PLUS );
        // 设置返回结果格式
        mRecognizer.setParameter(SpeechConstant.RESULT_TYPE, "json");

        // 设置语言
        mRecognizer.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        // 设置语言区域
        mRecognizer.setParameter(SpeechConstant.ACCENT, "mandarin");
        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mRecognizer.setParameter(SpeechConstant.VAD_BOS, "4000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mRecognizer.setParameter(SpeechConstant.VAD_EOS, "2000");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mRecognizer.setParameter(SpeechConstant.ASR_PTT, "1");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mRecognizer.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
        mRecognizer.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/iat.wav");
    }
    public void cancel(){
        if( null != mRecognizer ){
            mRecognizer.cancel();
            Log.e(TAG, "mRecognizer cancel: ==================取消识别，停止识别");
        }
    }
    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }
    public void destroy(){
        if( null != mRecognizer ){
            // 退出时释放连接
            mRecognizer.cancel();
            mRecognizer.destroy();
        }
        if ( SpeechUtility.getUtility()!=null) {
            SpeechUtility.getUtility().destroy();
        }
    }
}
