package com.plugin.myPlugin;

import android.content.Intent;
import android.util.Log;

import com.plugin.myPlugin.factory.IPluginAction;
import com.plugin.myPlugin.factory.PluginActionFactory;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class MyPlugin extends CordovaPlugin {
    private static final String TAG = "MyPlugin";
    IPluginAction mPluginAction;

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

        mPluginAction = PluginActionFactory.createPluginAction(action);
        if (mPluginAction == null) {
            return false;
        }
        mPluginAction.doAction(this, jsonObject, callbackContext);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d(TAG, "onActivityResult: " + requestCode + "resultCode" + resultCode);
        if (mPluginAction != null) {
            mPluginAction.onActivityResult(requestCode, resultCode, intent);
        }
        super.onActivityResult(requestCode, resultCode, intent);

    }

    /**
     * 处理运行时权限回调
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     * @throws JSONException
     */
    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        if (mPluginAction != null) {
            mPluginAction.onRequestPermissionResult(requestCode, permissions, grantResults);
        }
        super.onRequestPermissionResult(requestCode, permissions, grantResults);
    }


}
