package com.plugin.myPlugin.bean;

/**
 * author:  Allen <br>
 * date:  2017/8/23/023 14:49 <br>
 * description:
 */

public class LocationBean{
    /**
     * code : 错误码 1成功，0失败
     * msg : 描述
     * lng : 经度
     * lat : 经度
     * radius : 半径
     * province : 省
     * city : 市
     * county : 区、县
     * addr : 详情地址
     */

    private int code;
    private String msg;
    private double lng;
    private double lat;
    private int radius;
    private String province;
    private String city;
    private String county;
    private String addr;

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

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
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
}
