package com.yunlinker.ygsh;

import android.app.Application;

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
    }
}
