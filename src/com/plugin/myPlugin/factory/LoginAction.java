package com.plugin.myPlugin.factory;

import android.app.Activity;

import com.orhanobut.logger.Logger;
import com.plugin.myPlugin.utils.JsonWrapUtils;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by YX on 2017/8/20.
 *
 * {data:{code: 成功1，失败0
 msg: 描述
 openid:平台openid
 unionid:平台unionid
 headimgurl:用户头像url
 nickname:昵称
 sex:性别（男，女，未知）
 city:城市（如果没有就为空）}}
 */

public class LoginAction extends IPluginAction {
    private static final int LOGINTYPE_WECHAT = 1;
    private static final int LOGINTYPE_QQ = 2;
    private static final String TAG = "LoginAction";

    @Override
    public void doAction(CordovaPlugin plugin, JSONObject jsonObject, CallbackContext callbackContext) {
        int loginType = jsonObject.optInt("type");
        Logger.i("login type[1:wechat, 2:QQ] = " + loginType);

        switch (loginType) {
            case LOGINTYPE_WECHAT:
                umengAuthorize(plugin.cordova.getActivity(), SHARE_MEDIA.WEIXIN, callbackContext);
                break;
            case LOGINTYPE_QQ:
                umengAuthorize(plugin.cordova.getActivity(), SHARE_MEDIA.QQ, callbackContext);
                break;
            default:
                break;
        }
    }

    private void umengAuthorize(Activity context, SHARE_MEDIA shareMedia, final CallbackContext callbackContext) {
        UMShareAPI umShareAPI = UMShareAPI.get(context);
        umShareAPI.getPlatformInfo(context, shareMedia, new UMAuthListener() {
            @Override
            public void onStart(SHARE_MEDIA share_media) {

            }

            @Override
            public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> map) {
                //{unionid=, is_yellow_vip=0, screen_name=剑心, msg=, vip=0, city=成都,
                // accessToken=4BA5B59D33FCCBE438C948853AF13C81, gender=男, province=四川,
                // is_yellow_year_vip=0, openid=C9A1CC7DD7352E545EE9BF79A6B549E0,
                // profile_image_url=http://q.qlogo.cn/qqapp/1105543710/C9A1CC7DD7352E545EE9BF79A6B549E0/100,
                // yellow_vip_level=0, access_token=4BA5B59D33FCCBE438C948853AF13C81,
                // iconurl=http://q.qlogo.cn/qqapp/1105543710/C9A1CC7DD7352E545EE9BF79A6B549E0/100,
                // name=剑心, uid=C9A1CC7DD7352E545EE9BF79A6B549E0,
                // expiration=1511362939428, expires_in=1511362939428, level=0, ret=0}
                Logger.i(share_media + "授权成功:" + map);
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("code", 1);
                    jsonObject.put("msg", "授权成功!");
                    jsonObject.put("openid", map.get("openid"));
                    jsonObject.put("unionid", map.get("unionid"));
                    jsonObject.put("headimgurl", map.get("iconurl"));
                    jsonObject.put("nickname", map.get("name"));
                    jsonObject.put("sex", map.get("gender"));
                    jsonObject.put("city", map.get("city"));
                    callbackContext.success(JsonWrapUtils.wrapData(jsonObject));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
                try{
                    Logger.w(share_media + "授权失败", throwable);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("code", 0);
                    jsonObject.put("msg", "授权失败");
                    callbackContext.error(JsonWrapUtils.wrapData(jsonObject));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancel(SHARE_MEDIA share_media, int i) {
                try{
                    Logger.w(share_media + "授权取消");
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("code", 0);
                    jsonObject.put("msg", "授权取消");
                    callbackContext.error(JsonWrapUtils.wrapData(jsonObject));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }
}
