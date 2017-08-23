package com.plugin.myPlugin;

import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.plugin.myPlugin.bean.LocationBean;
import com.plugin.myPlugin.factory.IPluginAction;
import com.plugin.myPlugin.factory.PluginActionFactory;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.onekeyshare.OnekeyShare;
import com.yunlinker.ygsh.MediaPlayerActivity;

import java.io.Serializable;
import java.util.HashMap;

import static android.R.attr.data;

/**
 * This class echoes a string called from JavaScript.
 */
public class MyPlugin extends CordovaPlugin {
    private static final String TAG = "MyPlugin";
    private CallbackContext callbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        if (args.length() <= 0) {
            return false;
        }
        Log.i(TAG, "Plugin execute action = " + action + " , args = " + args.toString());
        JSONObject jsonObject = null;
        try {
            jsonObject = args.getJSONObject(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        IPluginAction pluginAction = PluginActionFactory.createPluginAction(action);
        if (pluginAction == null) {
            return false;
        }
        pluginAction.doAction(this,cordova, jsonObject, callbackContext);

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.d(TAG, "onActivityResult: "+requestCode+"resultCode"+resultCode);
        switch (requestCode) {
            case 1000:
                if (resultCode == 1) {
                    String data = intent.getStringExtra("data");
                    callbackContext.success(data);
                } else {
                    JSONObject jo = new JSONObject();
                    try {
                        jo.put("code", "0");
                        jo.put("msg", "定位失败");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    callbackContext.error(jo);
                }
                break;
        }
    }
}
