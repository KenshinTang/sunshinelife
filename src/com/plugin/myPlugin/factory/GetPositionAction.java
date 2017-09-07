package com.plugin.myPlugin.factory;

import android.content.Intent;
import android.util.Log;

import com.yunlinker.ygsh.MapLocationActivity;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by YX on 2017/8/20.
 */

public class GetPositionAction extends IPluginAction {
    private static final String TAG = "GetPositionAction";

    private static final int GET_LOCATION = 4;
    private CallbackContext mCallbackContext;

    @Override
    public void doAction(CordovaPlugin plugin, JSONObject jsonObject, CallbackContext callbackContext) {
        this.mCallbackContext = callbackContext;
        Intent intent = new Intent(plugin.cordova.getActivity(), MapLocationActivity.class);
        plugin.cordova.startActivityForResult(plugin, intent, GET_LOCATION);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case GET_LOCATION:
                if (resultCode == 1) {
                    String data = intent.getStringExtra("data");
                    try {
                        JSONObject jo = new JSONObject(data);
                        // 回调数据外面包一层data
                        JSONObject result = new JSONObject();
                        result.put("data", jo);
                        Log.i(TAG, "get position = " + result.toString());
                        mCallbackContext.success(result);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    JSONObject jo = new JSONObject();
                    // 回调数据外面包一层data
                    JSONObject result = new JSONObject();
                    try {
                        jo.put("code", "0");
                        jo.put("msg", "定位失败");
                        result.put("data", jo);
                        Log.i(TAG, "get position = " + result.toString());
                        mCallbackContext.error(result);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }
}
