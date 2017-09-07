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

import com.yunlinker.ygsh.R;
import com.yunlinker.ygsh.util.ToastUtil;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

/**
 * author:  Allen <br>
 * date:  2017/9/6 20:22<br>
 * description:
 */
public class NavigationAction extends IPluginAction {
    private static String[] paks = new String[]{"com.autonavi.minimap",//高德
            "com.baidu.BaiduMap"};     //百度
    private PopupWindow mPopupWindow;
    private Activity mActivity;

    /**
     * 调起百度客户端 路径规划
     * lat,lng (先纬度，后经度)
     * 40.057406655722,116.2964407172
     * lat,lng,lat,lng (先纬度，后经度, 先左下,后右上)
     *
     * @param activity
     */
    public static void openBaiduiDrectionMap(Context activity, String sLongitude, String sLatitude, String sName,
                                             String dLongitude, String dLatitude, String dName) {
        Intent intent = new Intent("android.intent.action.VIEW",
                android.net.Uri.parse("baidumap://map/direction?origin=name:" +
                        sName + "|latlng:" + sLatitude + "," + sLongitude + "&destination=name:" +
                        dName + "|latlng:" + dLatitude + "," + dLongitude + "&" +
                        "mode=transit&sy=0&index=0&target=0"));
        activity.startActivity(intent);
    }

    /**
     * 调起高德客户端 路径规划
     * lat,lng (先纬度，后经度)
     * 40.057406655722,116.2964407172
     * 输入起点和终点，搜索公交、驾车或步行的线路。支持版本 V4.2.1 起。
     *
     * @param activity
     * @param sLongitude
     * @param sLatitude
     * @param sName
     * @param dLongitude
     * @param dLatitude
     * @param dName
     * @param appName
     */
    public static void openGaodeRouteMap(Context activity, String sLongitude, String sLatitude, String sName,
                                         String dLongitude, String dLatitude, String dName, String appName) {
        Intent intent = new Intent("android.intent.action.VIEW",
                android.net.Uri.parse("amapuri://route/plan/?sourceApplication=" + appName +
                        "&sid=&slat=" + sLatitude + "&slon=" +
                        sLongitude + "&sname=" + sName + "&did=&dlat=" +
                        dLatitude + "&dlon=" + dLongitude + "&dname=" + dName + "&dev=1&t=1"));
        intent.setPackage("com.autonavi.minimap");
        activity.startActivity(intent);
    }

    /**
     * 打开网页版 导航
     *
     * @param activity
     * @param region   当给定region时，认为起点和终点都在同一城市，除非单独给定起点或终点的城市。
     * @param appName
     */
    public static void openBrosserNaviMap(Context activity, String sLongitude, String sLatitude,
                                          String sName, String dLongitude, String dLatitude, String dName, String region, String appName) {
        Uri mapUri = Uri.parse("http://api.map.baidu.com/direction?origin=latlng:" +
                sLatitude + "," + sLongitude + "|name:" + sName + "&destination=latlng:" +
                dLatitude + "," + dLongitude + "|name:" + dName + "&mode=driving&region=" + region +
                "&output=html&src=" + appName);
        Intent loction = new Intent(Intent.ACTION_VIEW, mapUri);
        activity.startActivity(loction);
    }

    /**
     * 通过包名获取应用信息
     *
     * @param context
     * @param packageName
     * @return
     */
    private static String getAppInfoByPak(Context context, String packageName) {
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
     *
     * @param context
     * @return
     */
    private static List<String> getMapApps(Context context) {
        LinkedList<String> apps = new LinkedList<>();
        for (String pak : paks) {
            String appinfo = getAppInfoByPak(context, pak);
            if (appinfo != null) {
                apps.add(appinfo);
            }
        }
        return apps;
    }

    @Override
    public void doAction(CordovaPlugin plugin, JSONObject jsonObject, CallbackContext callbackContext) {
        mActivity = plugin.cordova.getActivity();
//        final String dLat = jsonObject.optString("lat","30.52389372539968");
//        final String dLng = jsonObject.optString("lng","104.05925212021623");
//        final String dAddr = jsonObject.optString("addr","成都武侯区安公路二段274号");
        final String dLat = "30.52389372539968";
        final String dLng = "104.05925212021623";
        final String dAddr = "成都武侯区安公路二段274号";


        final String sLat = "30.50625321376835";
        final String sLng = "104.08463823392822";
        final String sAddr = "成都市双流县广东路1号";
        final List<String> mapApps = getMapApps(mActivity);
        if (mapApps != null && !mapApps.isEmpty()) {
            //有安装客户端 打开PopWindow显示数据
            if (mapApps.contains(paks[0]) || mapApps.contains(paks[1])) {
                LayoutInflater inflater = LayoutInflater.from(plugin.cordova.getActivity());
                View view = inflater.inflate(R.layout.popup_choose_map_layout, null);
                mPopupWindow = new PopupWindow(view,
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.WRAP_CONTENT);
                mPopupWindow.setFocusable(true);
                ColorDrawable dw = new ColorDrawable(0xb0000000);
                mPopupWindow.setBackgroundDrawable(dw);
                mPopupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);
                mPopupWindow.showAtLocation(plugin.cordova.getActivity().getCurrentFocus(),
                        Gravity.BOTTOM, 0, 0);
                TextView selectGaoDe = (TextView) view.findViewById(R.id.select_gaode);
                selectGaoDe.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mapApps.contains(paks[0])) {
                            openGaodeRouteMap(mActivity, sLng, sLat, sAddr, dLng, dLat, dAddr, "阳光生活");
                        } else {
                            ToastUtil.show(mActivity, "尚未安装高德地图，请尝试其他导航");
                        }
                    }
                });
                TextView selectBaiDu = (TextView) view.findViewById(R.id.select_baidu);
                selectBaiDu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mapApps.contains(paks[1])) {
                            openBaiduiDrectionMap(mActivity, sLng, sLat, sAddr, dLng, dLat, dAddr);
                        } else {
                            ToastUtil.show(mActivity, "尚未安装百度地图，请尝试其他导航");
                        }
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
        openBrosserNaviMap(mActivity, sLng, sLat, sAddr, dLng, dLat, dAddr, "成都市", "阳光生活");
    }
}
