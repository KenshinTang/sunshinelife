package com.plugin.myPlugin.factory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;

/**
 * Created by YX on 2017/8/20.
 * /*            回调 : {	code: 成功1，失败0
 msg: 描述
 unionid:平台ID
 face:用户头像url
 nikename:昵称
 sex:性别（男，女，未知）
 city:城市（如果没有就为空）}
 */

public class LoginAction implements IPluginAction {
    private static final String TAG = "LoginAction";

    public static final int LOGINTYPE_WECHAT = 1;
    public static final int LOGINTYPE_QQ = 2;

    @Override
    public void doAction(CordovaPlugin plugin, CordovaInterface cordova, JSONObject jsonObject, CallbackContext callbackContext) {
        int loginType = jsonObject.optInt("type");
        Log.i(TAG, "login type[1:wechat, 2:QQ] = " + loginType);
        loginType = LOGINTYPE_QQ;

        switch (loginType) {
            case LOGINTYPE_WECHAT:
//                MobAuthorize(new Wechat(), callbackContext);
                UmengAuthorize(cordova.getActivity(), SHARE_MEDIA.QQ, callbackContext);
                break;
            case LOGINTYPE_QQ:
//                MobAuthorize(new QQ(), callbackContext);
                UmengAuthorize(cordova.getActivity(), SHARE_MEDIA.WEIXIN, callbackContext);
                break;
            default:
                break;
        }
    }

    private void UmengAuthorize(Activity context, SHARE_MEDIA shareMedia, CallbackContext callbackContext) {
        UMShareAPI umShareAPI = UMShareAPI.get(context);
        umShareAPI.getPlatformInfo(context, shareMedia, new UMAuthListener() {
            @Override
            public void onStart(SHARE_MEDIA share_media) {

            }

            @Override
            public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> map) {
                Log.i(TAG, share_media + "授权成功:" + map);
            }

            @Override
            public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {

            }

            @Override
            public void onCancel(SHARE_MEDIA share_media, int i) {

            }
        });
    }

    private void MobAuthorize(Platform platform, final CallbackContext callbackContext) {
        String userId = platform.getDb().getUserId();
        if (!TextUtils.isEmpty(userId)) {
            platform.removeAccount(true);
        }
        platform.setPlatformActionListener(new PlatformActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                Log.w(TAG, platform.getName() + "授权成功" + hashMap);
                String openId = platform.getDb().getUserId();
                String userName = hashMap.get("nickname").toString();//ID
                String hearUrl = hashMap.get("headimgurl").toString();//ID
                String sex = hashMap.get("sex").toString();//ID
                String city = hashMap.get("city").toString();//ID

                int unionid = -1;
                if (platform instanceof Wechat) {
                    unionid = LOGINTYPE_WECHAT;
                } else if (platform instanceof QQ) {
                    unionid = LOGINTYPE_QQ;
                }
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("code", 1);
                    jsonObject.put("unionid", unionid);
                    jsonObject.put("face", hearUrl);
                    jsonObject.put("nikename", userName);
                    jsonObject.put("sex", userName);
                    jsonObject.put("city", city);
                    callbackContext.success(jsonObject);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {
                try{
                    Log.w(TAG, platform.getName() + "授权失败", throwable);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("code", 0);
                    jsonObject.put("msg", "授权失败");
                    callbackContext.error(jsonObject);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancel(Platform platform, int i) {
                try{
                    Log.w(TAG, platform.getName() + "授权取消");
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("code", 0);
                    jsonObject.put("msg", "授权取消");
                    callbackContext.error(jsonObject);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        platform.SSOSetting(false);
        platform.showUser(null);
    }
}
