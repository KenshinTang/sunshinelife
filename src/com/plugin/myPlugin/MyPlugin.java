package com.plugin.myPlugin;

import android.content.Intent;
import android.util.Log;

import com.yunlinker.ygsh.R;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;

import cn.sharesdk.onekeyshare.OnekeyShare;
import com.yunlinker.ygsh.MediaPlayerActivity;

/**
 * This class echoes a string called from JavaScript.
 */
public class MyPlugin extends CordovaPlugin {
    private static final String TAG = "MyPlugin";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Log.i(TAG, "Plugin execute action = " + action + " , args = " + args.toString());

        switch (action) {
            case "coolMethod":  //test
                playVideo();
                return true;
            case "upImgMethod":  //上传图片到阿里云
                return true;
            case "positionMethod":  //获取定位
                return true;
            case "shareUrlMethod":  //分享
                showShare();
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

    private void showShare() {
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();

        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间等使用
        oks.setTitle("标题");
        // titleUrl是标题的网络链接，QQ和QQ空间等使用
        oks.setTitleUrl("http://sharesdk.cn");
        // text是分享文本，所有平台都需要这个字段
        oks.setText("我是分享文本");
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        //oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片
        // url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl("http://sharesdk.cn");
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        oks.setComment("我是测试评论文本");
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite(cordova.getActivity().getString(R.string.app_name));
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl("http://sharesdk.cn");

        // 启动分享GUI
        oks.show(cordova.getActivity());
    }
}
