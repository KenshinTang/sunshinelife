package com.plugin.myPlugin.factory;

import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by YX on 2017/8/20.
 */

public class GetLocationAction extends IPluginAction {

    private LocationClient mLocationClient;

    //    {"code":1成功,0失败，"msg":"描述”,“lat”:”纬度” , “lng”: “经度”}
    @Override
    public void doAction(CordovaPlugin plugin, JSONObject jsonObject, final CallbackContext callbackContext) {
        mLocationClient = new LocationClient(plugin.cordova.getActivity());
        // 注册监听函数
        mLocationClient.registerLocationListener(new BDLocationListener() {
            //1.接收异步返回的定位结果，参数是BDLocation类型参数。
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                String lat = String.valueOf(bdLocation.getLatitude());
                String lng = String.valueOf(bdLocation.getLongitude());
                if (lat.equals("") || lng.equals("")) {
                    //定位失败
                    try {
                        JSONObject object = new JSONObject();
                        object.put("code", 0);
                        object.put("msg", "定位失败");
                        callbackContext.error(object);
                    } catch (JSONException e) {
                    }
                } else {
                    //定位成功
                    Log.d("allen", "onReceiveLocation: " + "经度：" + lat + "纬度：" + lng);
                    try {
                        JSONObject object = new JSONObject();
                        object.put("code", 1);
                        object.put("msg", "定位成功");
                        object.put("lat", lat);
                        object.put("lng", lng);
                        callbackContext.success(object);
                    } catch (Exception e) {
                    }
                }
            }
        });
        mLocationClient.start();
    }
}
