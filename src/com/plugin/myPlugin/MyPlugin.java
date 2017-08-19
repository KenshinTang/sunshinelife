package com.plugin.myPlugin;

import android.content.Intent;
import android.util.Log;

import com.yunlinker.ygsh.R;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.onekeyshare.OnekeyShare;
import com.yunlinker.ygsh.MediaPlayerActivity;

import java.util.HashMap;

/**
 * This class echoes a string called from JavaScript.
 */
public class MyPlugin extends CordovaPlugin {
    private static final String TAG = "MyPlugin";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (args.length() <= 0) {
            return false;
        }
        Log.i(TAG, "Plugin execute action = " + action + " , args = " + args.toString());
        JSONObject jsonObject = args.getJSONObject(0);

        switch (action) {
            case "coolMethod":  //test
                playVideo();
                return true;
            case "upImgMethod":  //上传图片到阿里云
                return true;
            case "positionMethod":  //获取定位
                return true;
            case "shareUrlMethod":  //分享
                showShare(jsonObject, callbackContext);
                return true;
            case "extLoginMethod":  //第三方登录
                return true;
            case "payMethod":  //支付
                return true;
            case "getPushTokenMethod":  //获取推送token
                return true;
        }

        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private void playVideo() {
        Intent intent = new Intent(cordova.getActivity(), MediaPlayerActivity.class);
        cordova.getActivity().startActivity(intent);
    }

    private void showShare(final JSONObject jsonObject, final CallbackContext callbackContext) {
        // [{"pic":"https:\/\/m.baidu.com\/static\/index\/plus\/plus_logo.png",
        // "title":"ceshititle",
        // "desc":"ceshidesc",
        // "url":"https:\/\/www.baidu.com\/"}]
        try {
            String title = jsonObject.getString("title");
            String imagePath = jsonObject.getString("pic");
            String desc = jsonObject.getString("desc");
            String url = jsonObject.getString("url");

            Log.i(TAG, "show Share [title:" + title + ", imagePath:" + imagePath + ", desc:" + desc + ", url:" + url + "]");

            OnekeyShare oks = new OnekeyShare();
            //关闭sso授权
            oks.disableSSOWhenAuthorize();

            // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间等使用
            oks.setTitle(title);
            // titleUrl是标题的网络链接，QQ和QQ空间等使用
            oks.setTitleUrl(url);
            // text是分享文本，所有平台都需要这个字段
            oks.setText(desc);
            // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
            oks.setImagePath(imagePath);//确保SDcard下面存在此张图片
            // url仅在微信（包括好友和朋友圈）中使用
            oks.setUrl(url);
            // comment是我对这条分享的评论，仅在人人网和QQ空间使用
            oks.setComment(desc);
            // site是分享此内容的网站名称，仅在QQ空间使用
//            oks.setSite(cordova.getActivity().getString(R.string.app_name));
            // siteUrl是分享此内容的网站地址，仅在QQ空间使用
//            oks.setSiteUrl("http://sharesdk.cn");

            oks.setCallback(new PlatformActionListener() {
                @Override
                public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                    try{
                        Log.i(TAG, "platform = " + platform + ", i = " + i + "hashmap = " + hashMap);
                        JSONObject jo = new JSONObject();
                        jo.put("code","1");
                        jo.put("msg","分享成功");
                        callbackContext.success(jo);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(Platform platform, int i, Throwable throwable) {
                    try{
                        Log.w(TAG, "platform = " + platform + ", i = " + i, throwable);
                        JSONObject jo = new JSONObject();
                        jo.put("code","0");
                        jo.put("msg","分享失败");
                        callbackContext.error(jo);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancel(Platform platform, int i) {
                    try{
                        Log.w(TAG, "platform = " + platform + ", i = " + i);
                        JSONObject jo = new JSONObject();
                        jo.put("code","0");
                        jo.put("msg","分享取消");
                        callbackContext.error(jo);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });

            // 启动分享GUI
            oks.show(cordova.getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
