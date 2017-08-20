package com.plugin.myPlugin.factory;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.json.JSONObject;

/**
 * Created by YX on 2017/8/20.
 */

public interface IPluginAction {
    void doAction(CordovaInterface cordova, JSONObject jsonObject, CallbackContext callbackContext);
}
