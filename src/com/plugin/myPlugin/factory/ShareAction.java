package com.plugin.myPlugin.factory;

import android.Manifest;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

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

public class ShareAction extends IPluginAction {
    private static final String TAG = "ShareAction";

    // [{"pic":"https:\/\/m.baidu.com\/static\/index\/plus\/plus_logo.png",
    // "title":"ceshititle",
    // "desc":"ceshidesc",
    // "url":"https:\/\/www.baidu.com\/"}]
    @Override
    public void doAction(CordovaPlugin plugin, JSONObject jsonObject, final CallbackContext callbackContext) {
        String title = jsonObject.optString("title");
        String imagePath = jsonObject.optString("pic");
        String desc = jsonObject.optString("desc");
        String url = jsonObject.optString("url");

        Log.i(TAG, "show Share [title:" + title + ", imagePath:" + imagePath + ", desc:" + desc + ", url:" + url + "]");

        //友盟分享
        showUmengShare(plugin.cordova, title, url, desc, imagePath, callbackContext);

        //Mob分享
//        showOnekeyShare(cordova, title, url, desc, imagePath, callbackContext);
    }

    private void showUmengShare(CordovaInterface cordova, String title, String url, String desc, String imagePath, final CallbackContext callbackContext) {
        //UMeng分享的SDK版本适配, 动态申请权限
        if (Build.VERSION.SDK_INT >= 23) {
            String[] mPermissionList = new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.READ_LOGS,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.SET_DEBUG_APP,
                    Manifest.permission.SYSTEM_ALERT_WINDOW,
                    Manifest.permission.GET_ACCOUNTS,
                    Manifest.permission.WRITE_APN_SETTINGS};
            ActivityCompat.requestPermissions(cordova.getActivity(), mPermissionList, 123);
        }

        UMWeb  web = new UMWeb(url);
        web.setTitle(title);//标题
        web.setThumb(new UMImage(cordova.getActivity(), imagePath));
        web.setDescription(desc);//描述

        new com.umeng.socialize.ShareAction(cordova.getActivity())
                .withMedia(web)
                .setDisplayList(SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE, SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE)
                .setCallback(new UMShareListener() {
                    @Override
                    public void onStart(SHARE_MEDIA share_media) {

                    }

                    @Override
                    public void onResult(SHARE_MEDIA share_media) {
                        try {
                            Log.i(TAG, "分享成功 onResult SHARE_MEDIA = " + share_media);
                            JSONObject jo = new JSONObject();
                            jo.put("code", "1");
                            jo.put("msg", "分享成功");
                            callbackContext.success(jo);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(SHARE_MEDIA share_media, Throwable throwable) {
                        try {
                            Log.w(TAG, "分享失败 onError SHARE_MEDIA = " + share_media, throwable);
                            JSONObject jo = new JSONObject();
                            jo.put("code", "0");
                            jo.put("msg", "分享失败");
                            callbackContext.error(jo);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancel(SHARE_MEDIA share_media) {
                        try {
                            Log.w(TAG, "分享取消 onCancel SHARE_MEDIA = " + share_media);
                            JSONObject jo = new JSONObject();
                            jo.put("code", "0");
                            jo.put("msg", "分享取消");
                            callbackContext.error(jo);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
                .open();
    }

    private void showOnekeyShare(CordovaInterface cordova, String title, String url, String desc, String imagePath, final CallbackContext callbackContext) {
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
