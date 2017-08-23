package com.yunlinker.ygsh;

import android.Manifest;
import android.app.Application;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.mob.MobSDK;
import com.umeng.socialize.Config;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.utils.ContextUtil;

/**
 * Created by YX on 2017/8/19.
 * 18011613272   111222
 */

public class YunApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
//        initMobSDK();
        initUmengSDK();
        initBaiduSDK();
    }

    private void initMobSDK() {
        //初始化一键分享SDK
        MobSDK.init(this, "1fe4899e8e58d", "8b76d42bc1e794a19b8864ee00010ed1");
    }

    private void initBaiduSDK() {
        // 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
        SDKInitializer.initialize(this);
        //自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
        //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
        SDKInitializer.setCoordType(CoordType.BD09LL);
    }

    private void initUmengSDK() {
        Config.DEBUG = true;
        ContextUtil.setContext(this);
        PlatformConfig.setWeixin("","");
        PlatformConfig.setQQZone("1105543710","WcTA1g0pEeT50ccg");
    }
}


