package com.yunlinker.ygsh;

import android.app.Application;
import android.os.StrictMode;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.plugin.myPlugin.factory.PluginActionFactory;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.socialize.Config;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.utils.ContextUtil;

/**
 * Created by YX on 2017/8/19.
 * 18011613272   111222
 * 18011613271   111222
 * 18683253663   qazwsx
 */

public class YunApplication extends Application {
    private static final String TAG = "YunApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        initUmengSDK();
//        initBaiduSDK();
        PluginActionFactory.initProperties();
        // android 7.0系统解决拍照的问题
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
        Logger.addLogAdapter(new AndroidLogAdapter());
    }

    private void initBaiduSDK() {
        // 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
        SDKInitializer.initialize(this);
        //自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
        //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
        SDKInitializer.setCoordType(CoordType.BD09LL);
    }

    private void initUmengSDK() {
        //分享和登录的SDK
        Config.DEBUG = true;
        ContextUtil.setContext(this);
        //PlatformConfig.setWeixin("wxd3769510490f1c98", "210b2c1a2208a804adeda8b14ee5dcb7");  //没有支付功能
        PlatformConfig.setWeixin("wxd46fd7b11f8fb67f", "6c5f2d134ad41ae80424c4d1e75ee36a");
        PlatformConfig.setQQZone("1105543710", "WcTA1g0pEeT50ccg");

        //消息推送的SDK
        PushAgent.getInstance(this).register(new IUmengRegisterCallback() {
            @Override
            public void onSuccess(String s) {
                Logger.i("UMeng push register success, device token = " + s);
            }

            @Override
            public void onFailure(String s, String s1) {
                Logger.w("UMeng push register failed, " + s + ", " + s1);
            }
        });
    }
}


