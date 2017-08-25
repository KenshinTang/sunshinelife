package com.plugin.myPlugin.factory;

import com.plugin.myPlugin.Properties;

/**
 * Created by YX on 2017/8/20.
 */

public class PluginActionFactory {
    public static void initProperties() {
        Properties.addProperties("upImgMethod", "com.plugin.myPlugin.factory.UploadImgAction");//上传图片
        Properties.addProperties("positionMethod", "com.plugin.myPlugin.factory.GetPositionAction"); //获取定位
        Properties.addProperties("shareUrlMethod", "com.plugin.myPlugin.factory.ShareAction");//分享
        Properties.addProperties("extLoginMethod", "com.plugin.myPlugin.factory.LoginAction");//第三方登录
        Properties.addProperties("payMethod", "com.plugin.myPlugin.factory.PayAction"); //支付
        Properties.addProperties("getPushTokenMethod", "com.plugin.myPlugin.factory.GetPushTokenAction"); //获取推送token
        Properties.addProperties("coolMethod", "com.plugin.myPlugin.factory.TestAction");//test
        Properties.addProperties("getLatLngMethod", "com.plugin.myPlugin.factory.GetLocationAction");//获取经纬度
    }

    public static IPluginAction createPluginAction(String action) {
        IPluginAction pluginAction = null;
        try {
            String fullName = Properties.getProperties(action);
            pluginAction = (IPluginAction) Class.forName(fullName).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pluginAction;
    }
}
