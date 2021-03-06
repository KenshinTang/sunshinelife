package com.plugin.myPlugin.factory;

import android.text.TextUtils;

import com.orhanobut.logger.Logger;
import com.plugin.myPlugin.utils.JsonWrapUtils;
import com.umeng.message.PushAgent;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONObject;

/**
 * Created by YX on 2017/8/20.
 */

public class GetPushTokenAction extends IPluginAction {
    //{data:{"code":1成功,0失败，"msg":"描述”,”type","友盟、极光 推送名字，如果未获取或者没有推送端则为空", devicetype:(1安卓, 2苹果), devicetoken: “”}}
    @Override
    public void doAction(CordovaPlugin plugin, JSONObject jsonObject, CallbackContext callbackContext) {
        String token = PushAgent.getInstance(plugin.cordova.getActivity()).getRegistrationId();
        Logger.i("UMeng Push Token :" + token);
        boolean isValid = checkToken(token);
        try {
            JSONObject jo = new JSONObject();
            jo.put("code", isValid ? 1 : 0);
            jo.put("msg", isValid ? "获取推送Token成功" : "获取推送Token失败");
            jo.put("type", "友盟");
            jo.put("devicetype", 1);
            jo.put("devicetoken", token);
            JSONObject result = JsonWrapUtils.wrapData(jo);
            if (isValid) {
                callbackContext.success(result);
            } else {
                callbackContext.error(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkToken(String token) {
        //UMeng的推送Token, 长度固定为44位
        return !TextUtils.isEmpty(token) && token.length() == 44;
    }
}
