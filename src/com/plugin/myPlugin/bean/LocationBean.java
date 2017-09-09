package com.plugin.myPlugin.bean;

import android.text.TextUtils;

import java.io.Serializable;

/**
 * author:  Allen <br>
 * date:  2017/8/23/023 14:49 <br>
 * description:
 */

public class LocationBean implements Serializable{
    /**
     * code : 0
     * msg : 描述
     * location_name : 地址名
     * location_adress : 地址
     * location_lat : 纬度
     * location_lng : 经度
     * province : 省
     * city : 市
     * county : 区、县
     * addr : 详情地址
     */

    private int code;
    private String msg;
    private String location_name;
    private String location_adress;
    private double location_lat;
    private double location_lng;
    private String province;
    private String city;
    private String county;
    private String addr;

    public LocationBean() {

    }

    public LocationBean(Double lat, Double lng, String address) {
        location_adress = address;
        location_lat = lat;
        location_lng = lng;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getLocation_name() {
        return location_name;
    }

    public void setLocation_name(String location_name) {
        this.location_name = location_name;
    }

    public String getLocation_adress() {
        return location_adress;
    }

    public void setLocation_adress(String location_adress) {
        this.location_adress = location_adress;
    }

    public double getLocation_lat() {
        return location_lat;
    }

    public void setLocation_lat(double location_lat) {
        this.location_lat = location_lat;
    }

    public double getLocation_lng() {
        return location_lng;
    }

    public void setLocation_lng(double location_lng) {
        this.location_lng = location_lng;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public boolean isValid() {
        return location_lat != 0.0d && location_lng != 0.0d && !TextUtils.isEmpty(location_adress);
    }

    @Override
    public String toString() {
        return location_adress + "(" + location_lat + ", " + location_lng + ")";
    }
}
