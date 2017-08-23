package com.plugin.myPlugin.factory;

import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONObject;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.onekeyshare.OnekeyShare;

/**
 * Created by YX on 2017/8/20.
 */

public class ShareAction implements IPluginAction {
    private static final String TAG = "ShareAction";

    // [{"pic":"https:\/\/m.baidu.com\/static\/index\/plus\/plus_logo.png",
    // "title":"ceshititle",
    // "desc":"ceshidesc",
    // "url":"https:\/\/www.baidu.com\/"}]
    @Override
    public void doAction(CordovaPlugin plugin, CordovaInterface cordova, JSONObject jsonObject, final CallbackContext callbackContext) {
        String title = jsonObject.optString("title");
        String imagePath = jsonObject.optString("pic");
        String desc = jsonObject.optString("desc");
        String url = jsonObject.optString("url");

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
                try {
                    Log.i(TAG, "platform = " + platform + ", i = " + i + "hashmap = " + hashMap);
                    JSONObject jo = new JSONObject();
                    jo.put("code", "1");
                    jo.put("msg", "分享成功");
                    callbackContext.success(jo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Platform platform, int i, Throwable throwable) {
                try {
                    Log.w(TAG, "platform = " + platform + ", i = " + i, throwable);
                    JSONObject jo = new JSONObject();
                    jo.put("code", "0");
                    jo.put("msg", "分享失败");
                    callbackContext.error(jo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancel(Platform platform, int i) {
                try {
                    Log.w(TAG, "platform = " + platform + ", i = " + i);
                    JSONObject jo = new JSONObject();
                    jo.put("code", "0");
                    jo.put("msg", "分享取消");
                    callbackContext.error(jo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // 启动分享GUI
        oks.show(cordova.getActivity());
    }
}
