package com.plugin.myPlugin;

import android.content.Intent;
import android.util.Log;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.ionic.ygsh_y.MediaPlayerActivity;

/**
 * This class echoes a string called from JavaScript.
 */
public class MyPlugin extends CordovaPlugin {
    private static final String TAG = "MyPlugin";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Log.i(TAG, "Plugin execute action = " + action + " , args = " + args.toString());

        switch (action) {
            case "coolMethod":  //test
                playVideo("");
                return true;
            case "upImgMethod":  //上传图片到阿里云
                return true;
            case "positionMethod":  //获取定位
                return true;
            case "shareUrlMethod":  //分享
                return true;
            case "extLoginMethod":  //第三方登录
                return true;
            case "payMethod":  //支付
                return true;
            case "getPushTokenMethod":  //获取推送token
                return true;
        }

        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private void playVideo(String url) {
        Intent intent = new Intent(cordova.getActivity(), MediaPlayerActivity.class);
        intent.putExtra("url", url);
        cordova.getActivity().startActivity(intent);
    }
}
