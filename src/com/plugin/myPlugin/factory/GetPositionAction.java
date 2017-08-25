package com.plugin.myPlugin.factory;

import android.content.Intent;

import com.yunlinker.ygsh.MapLocationActivity;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONObject;

/**
 * Created by YX on 2017/8/20.
 */

public class GetPositionAction implements IPluginAction {

    @Override
    public void doAction(CordovaPlugin plugin, CordovaInterface cordova, JSONObject jsonObject, CallbackContext callbackContext) {
        Intent intent = new Intent(cordova.getActivity(), MapLocationActivity.class);
        cordova.startActivityForResult(plugin,intent,1000);
    }
}
