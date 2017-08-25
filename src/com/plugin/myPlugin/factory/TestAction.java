package com.plugin.myPlugin.factory;

import android.content.Intent;

import com.yunlinker.ygsh.MediaPlayerActivity;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONObject;

/**
 * Created by YX on 2017/8/20.
 */

public class TestAction extends IPluginAction {

    @Override
    public void doAction(CordovaPlugin plugin, JSONObject jsonObject, CallbackContext callbackContext) {
        Intent intent = new Intent(plugin.cordova.getActivity(), MediaPlayerActivity.class);
        plugin.cordova.getActivity().startActivity(intent);
    }
}
