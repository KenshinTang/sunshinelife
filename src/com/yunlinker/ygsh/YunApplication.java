package com.yunlinker.ygsh;

import android.app.Application;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.mob.MobSDK;

/**
 * Created by YX on 2017/8/19.
 * 18011613272   111222
 */

public class YunApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //初始化一键分享SDK
        MobSDK.init(this, "1fe4899e8e58d", "8b76d42bc1e794a19b8864ee00010ed1");
        // 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
        SDKInitializer.initialize(this);
        //自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
        //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
        SDKInitializer.setCoordType(CoordType.BD09LL);
    }
}
