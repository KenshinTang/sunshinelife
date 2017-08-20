package com.plugin.myPlugin.factory;

import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by YX on 2017/8/20.
 */

public class LoginAction implements IPluginAction {
    private static final String TAG = "LoginAction";

    public static final int LOGINTYPE_WECHAT = 1;
    public static final int LOGINTYPE_QQ = 2;

    @Override
    public void doAction(CordovaInterface cordova, JSONObject jsonObject, CallbackContext callbackContext) {
        int loginType = jsonObject.optInt("type");
        Log.i(TAG, "login type[1:wechat, 2:QQ] = " + loginType);
        switch (loginType) {
            case LOGINTYPE_WECHAT:
                break;
            case LOGINTYPE_QQ:
                break;
            default:
                break;
        }
    }
}
