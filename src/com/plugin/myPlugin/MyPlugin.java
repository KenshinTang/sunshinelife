package com.plugin.myPlugin;

import android.content.Intent;
import android.util.Log;

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

import java.util.HashMap;

/**
 * This class echoes a string called from JavaScript.
 */
public class MyPlugin extends CordovaPlugin {
    private static final String TAG = "MyPlugin";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
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
//        Log.d(TAG, "onActivityResult: "+requestCode+"resultCode"+resultCode+"extra"+intent.getStringExtra("address"));
        Log.d(TAG, "onActivityResult: "+requestCode+"resultCode"+resultCode);
        switch (requestCode) {
            case 1000:
                if (resultCode == 1) {
                    // TODO: 2017/8/23 解析
                } else {

                }
                break;
        }
    }
}
