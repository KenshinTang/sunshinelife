package com.plugin.myPlugin.factory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.orhanobut.logger.Logger;
import com.plugin.myPlugin.bean.LocationBean;
import com.plugin.myPlugin.utils.JsonWrapUtils;
import com.plugin.myPlugin.utils.SharedPreferencesHelper;
import com.yunlinker.ygsh.R;
import com.yunlinker.ygsh.util.ToastUtil;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

/**
 * author:  Allen <br>
 * date:  2017/9/6 20:22<br>
 * description: {data:{"code":1成功,0失败，"msg":"描述”}}
 */
public class NavigationAction extends IPluginAction {
    private static final String[] paks = new String[]{"com.autonavi.minimap",//高德
                                                "com.baidu.BaiduMap"};     //百度
    private PopupWindow mPopupWindow;
    private CordovaPlugin mPlugin;
    private JSONObject mData;
    private CallbackContext mCallbackContext;
    private LocationBean mDeparture;
    private LocationBean mDstination;

    @Override
    public void doAction(final CordovaPlugin plugin, JSONObject jsonObject, CallbackContext callbackContext) {
        mPlugin = plugin;
        mData = jsonObject;
        mCallbackContext = callbackContext;

        startNavigation();
    }

    private void startNavigation() {
        // 目的地
        final Double dsLat = mData.optDouble("lat", 0.0d);
        final Double dsLng = mData.optDouble("lng", 0.0d);
        final String dsAddr = mData.optString("addr", "");
        mDstination = new LocationBean(dsLat, dsLng, dsAddr);

        // 出发地
        SharedPreferencesHelper helper = new SharedPreferencesHelper(mPlugin.cordova.getActivity());
        final Double deLat = Double.parseDouble(helper.getString("lat", "0"));
        final Double deLng = Double.parseDouble(helper.getString("lng", "0"));
        final String deAddr = helper.getString("addr", "");
        mDeparture = new LocationBean(deLat, deLng, deAddr);

        if (!mDeparture.isValid()) {
            LocationClient locationClient = new LocationClient(mPlugin.cordova.getActivity());
            // 注册监听函数
            locationClient.registerLocationListener(new BDLocationListener() {
                //1.接收异步返回的定位结果，参数是BDLocation类型参数。
                @Override
                public void onReceiveLocation(BDLocation bdLocation) {
                    mDeparture.setAddr(bdLocation.getAddrStr());
                    mDeparture.setLocation_lat(bdLocation.getLatitude());
                    mDeparture.setLocation_lng(bdLocation.getLongitude());
                    openNavigationApp();
                }
            });
            locationClient.start();
        } else {
            openNavigationApp();
        }
    }

    private void openNavigationApp() {
        Logger.i("导航: " + mDeparture.toString() + " ------> " + mDstination.toString());
        final Activity activity = mPlugin.cordova.getActivity();
        final List<String> mapApps = getMapApps(activity);
        if (mapApps != null && !mapApps.isEmpty()) {
            //有安装客户端 打开PopWindow显示数据
            if (mapApps.contains(paks[0]) || mapApps.contains(paks[1])) {
                LayoutInflater inflater = LayoutInflater.from(mPlugin.cordova.getActivity());
                View view = inflater.inflate(R.layout.popup_choose_map_layout, null);
                mPopupWindow = new PopupWindow(view,
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.WRAP_CONTENT);
                mPopupWindow.setFocusable(true);
                ColorDrawable dw = new ColorDrawable(0xb0000000);
                mPopupWindow.setBackgroundDrawable(dw);
                mPopupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);
                mPopupWindow.showAtLocation(activity.getCurrentFocus(),
                        Gravity.BOTTOM, 0, 0);
                TextView selectGaoDe = (TextView) view.findViewById(R.id.select_gaode);
                selectGaoDe.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mapApps.contains(paks[0])) {
                            openGaodeRouteMap(activity, mDeparture, mDstination, "阳光生活");
                        } else {
                            ToastUtil.show(activity, "尚未安装高德地图，请尝试其他导航");
                        }
                        mPopupWindow.dismiss();
                    }
                });
                TextView selectBaiDu = (TextView) view.findViewById(R.id.select_baidu);
                selectBaiDu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mapApps.contains(paks[1])) {
                            openBaiduiDrectionMap(activity, mDeparture, mDstination);
                        } else {
                            ToastUtil.show(activity, "尚未安装百度地图，请尝试其他导航");
                        }
                        mPopupWindow.dismiss();
                    }
                });
                TextView cancel = (TextView) view.findViewById(R.id.cancel);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //取消
                        mPopupWindow.dismiss();
                    }
                });
                return;
            }
        }
        //没有安装客户端 打开网页版 手机浏览器发现必传region才能正确打开导航
        openBrosserNaviMap(activity, mDeparture, mDstination, "成都市", "阳光生活");
    }

    /**
     * 调起百度客户端 路径规划
     * lat,lng (先纬度，后经度)
     * 40.057406655722,116.2964407172
     * lat,lng,lat,lng (先纬度，后经度, 先左下,后右上)
     */
    private static void openBaiduiDrectionMap(Context activity, LocationBean departure, LocationBean dstination) {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                android.net.Uri.parse("baidumap://map/direction?origin=name:" +
                        dstination.getLocation_adress() + "|latlng:" + dstination.getLocation_lat() +
                        "," + dstination.getLocation_lng() + "&destination=name:" +
                        departure.getLocation_adress() + "|latlng:" + departure.getLocation_lat() +
                        "," + departure.getLocation_lng() + "&" +
                        "mode=transit&sy=0&index=0&target=0"));
        activity.startActivity(intent);
    }

    /**
     * 调起高德客户端 路径规划
     * lat,lng (先纬度，后经度)
     * 40.057406655722,116.2964407172
     * 输入起点和终点，搜索公交、驾车或步行的线路。支持版本 V4.2.1 起。
     */
    private static void openGaodeRouteMap(Activity activity, LocationBean departure, LocationBean dstination, String appName) {
        Uri mapUri = Uri.parse("amapuri://route/plan/?sourceApplication=" + appName +
                "&sid=&slat=" + departure.getLocation_lat() +
                "&slon=" + departure.getLocation_lng() +
                "&sname=" + departure.getLocation_adress() +
                "&did=&dlat=" + dstination.getLocation_lat() +
                "&dlon=" + dstination.getLocation_lng() +
                "&dname=" + dstination.getLocation_adress() +
                "&dev=1&t=1");
        Intent intent = new Intent(Intent.ACTION_VIEW, mapUri);
        intent.setPackage("com.autonavi.minimap");
        activity.startActivity(intent);
    }

    /**
     * 打开网页版 导航
     */
    private static void openBrosserNaviMap(Activity activity, LocationBean departure, LocationBean dstination, String region, String appName) {
        Uri mapUri = Uri.parse("http://api.map.baidu.com/direction?origin=latlng:" +
                departure.getLocation_lat() + "," + departure.getLocation_lng() +
                "|name:" + departure.getLocation_adress() +
                "&destination=latlng:" + dstination.getLocation_lat() + "," +
                dstination.getLocation_lng() + "|name:" +
                dstination.getLocation_adress() + "&mode=driving&region=" + region +
                "&output=html&src=" + appName);
        Intent intent = new Intent(Intent.ACTION_VIEW, mapUri);
        activity.startActivity(intent);
    }

    /**
     * 通过包名获取应用信息
     */
    private static String getAppInfoByPak(Activity context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        for (PackageInfo packageInfo : packageInfos) {
            if (packageName.equals(packageInfo.packageName)) {
                return packageName;
            }
        }
        return null;
    }

    /**
     * 返回当前设备上的地图应用集合
     */
    private static List<String> getMapApps(Activity context) {
        LinkedList<String> apps = new LinkedList<>();
        for (String pak : paks) {
            String appinfo = getAppInfoByPak(context, pak);
            if (appinfo != null) {
                apps.add(appinfo);
            }
        }
        return apps;
    }
}
