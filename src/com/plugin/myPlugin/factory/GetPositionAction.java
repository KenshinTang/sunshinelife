package com.plugin.myPlugin.factory;

import android.content.Intent;

import com.yunlinker.ygsh.MapLocationActivity;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by YX on 2017/8/20.
 */

public class GetPositionAction extends IPluginAction {
    public static final int GET_LOCATION = 4;
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
                        mCallbackContext.success(jo);
                    } catch (JSONException e) {
                    }
                } else {
                    JSONObject jo = new JSONObject();
                    try {
                        jo.put("code", "0");
                        jo.put("msg", "定位失败");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mCallbackContext.error(jo);
                }
                break;
        }
    }
}
