package com.plugin.myPlugin.factory;

import android.content.Intent;

import com.yunlinker.ygsh.MediaPlayerActivity;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.json.JSONObject;

/**
 * Created by YX on 2017/8/20.
 */

public class TestAction implements IPluginAction {

    @Override
    public void doAction(CordovaInterface cordova, JSONObject jsonObject, CallbackContext callbackContext) {
        Intent intent = new Intent(cordova.getActivity(), MediaPlayerActivity.class);
        cordova.getActivity().startActivity(intent);
    }
}
