package com.plugin.myPlugin.factory;

/**
 * Created by YX on 2017/8/20.
 */

public class PluginActionFactory {
    public static IPluginAction createPluginAction(String action) {
        IPluginAction pluginAction = null;
        switch (action) {
            case "upImgMethod": //上传图片
                pluginAction = new UploadImgAction();
                break;
            case "positionMethod":  //获取定位
                pluginAction = new GetPositionAction();
                break;
            case "shareUrlMethod":  //分享
                pluginAction = new ShareAction();
                break;
            case "extLoginMethod":  //第三方登录
                pluginAction = new LoginAction();
                break;
            case "payMethod":  //支付
                pluginAction = new PayAction();
                break;
            case "getPushTokenMethod":  //获取推送token
                pluginAction = new GetPushTokenAction();
                break;
            case "coolMethod":  //test
                pluginAction = new TestAction();
                break;
        }

        return pluginAction;
    }
}
