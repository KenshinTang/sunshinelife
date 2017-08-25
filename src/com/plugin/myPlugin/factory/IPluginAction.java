package com.plugin.myPlugin.factory;

import android.content.Intent;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by YX on 2017/8/20.
 */

public abstract class IPluginAction {
    public abstract void doAction(CordovaPlugin plugin, JSONObject jsonObject, CallbackContext callbackContext);

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
    }
}
