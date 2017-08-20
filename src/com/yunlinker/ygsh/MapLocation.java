package com.yunlinker.ygsh;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.yunlinker.ygsh.util.ToastUtil;
import com.yunlinker.ygsh.view.SearchEditView;

import java.util.ArrayList;
import java.util.List;


/**
 * author:  Allen <br>
 * date:  2017/8/19 16:33<br>
 * description:
 */
public class MapLocation extends Activity implements OnGetPoiSearchResultListener, BaiduMap.OnMapStatusChangeListener ,OnGetGeoCoderResultListener {

    private Context mContext;

    private MapView mMapView = null;
    private BaiduMap mBaiduMap;

    private TextView mSendButton;
    private Button mRequestLocation;
    private ListView mListView;

    // 搜索周边相关
    private PoiSearch mPoiSearch = null;
    private SuggestionSearch mSuggestionSearch = null;

    /**
     * 定位SDK的核心类
     */
    public LocationClient mLocationClient = null;

    /**
     * 当前标志
     */
    private Marker mCurrentMarker;
    // 定位图标描述
    private BitmapDescriptor currentMarker = null;

    public BDLocationListener myListener = new MyLocationListener();

    private List<PoiInfo> dataList;
    private ListAdapter adapter;

    private double longitude;// 精度
    private double latitude;// 维度
    private float radius;// 定位精度半径，单位是米
    private int locType;
    private String addrStr;// 反地理编码
    private String province;// 省份信息
    private String city;// 城市信息
    private String district;// 区县信息
    private float direction;// 手机方向信息
    private ListView mSearchPoisList;
    private LatLng locationLatLng;
    private RelativeLayout mapHeadView;
    private RelativeLayout mapLayout;

    /**
     * 构造广播监听类，监听 SDK key 验证以及网络异常广播
     */
    public class SDKReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            String s = intent.getAction();
            if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
                Log.d("allen", "key 验证出错! 错误码 :" + intent.getIntExtra
                        (SDKInitializer.SDK_BROADTCAST_INTENT_EXTRA_INFO_KEY_ERROR_CODE, 0)
                        + " ; 请在 AndroidManifest.xml 文件中检查 key 设置");
            } else if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK)) {
                Log.d("allen", "key 验证成功! 功能可以正常使用");
            } else if (s.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
                Log.d("allen", "网络出错");
            }
        }
    }

    private SDKReceiver mReceiver;
    
    private int checkPosition;
    private SearchEditView mSearchEditView;
    private GeoCoder mSearch;
    private boolean mMove;
    @Override
    public void onMapStatusChangeStart(MapStatus mapStatus) {
        Log.d("allen","onMapStatusChangeStart");
        mMove = true;
    }

    @Override
    public void onMapStatusChange(MapStatus mapStatus) {
        Log.d("allen","onMapStatusChange");
    }

    @Override
    public void onMapStatusChangeFinish(MapStatus mapStatus) {
        Log.d("allen","onMapStatusChangeFinish");
        if (mMove) {
            //地图操作的中心点
            LatLng ptCenter = mapStatus.target;
            mSearch.reverseGeoCode(new ReverseGeoCodeOption()
                    .location(ptCenter));
            mMove = false;
        }
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
        Log.d("allen","onGetGeoCodeResult");
    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
        Log.d("allen","onGetReverseGeoCodeResult");
        List<PoiInfo> poiInfos = reverseGeoCodeResult.getPoiList();
        if (poiInfos.isEmpty()) {
            return;
        }
        dataList.clear();
        dataList.addAll(poiInfos);
        checkPosition = 0;
        adapter.setCheckPosition(0);
        adapter.notifyDataSetChanged();
        mListView.setSelection(0);
        LatLng location = poiInfos.get(0).location;
//        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(location);
//        mBaiduMap.animateMapStatus(u);
        mCurrentMarker.setPosition(location);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baidumap);
        mContext = this;

        // 注册 SDK 广播监听者
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK);
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
        mReceiver = new SDKReceiver();
        registerReceiver(mReceiver, iFilter);

        initView();
        initEvent();

        checkPermission();
    }

    private void checkPermission() {
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(android.Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(this, permissions, 1);
        } else {
            initLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    initLocation();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    private void hideMapView() {
        mapHeadView.setVisibility(View.GONE);
        mapLayout.setVisibility(View.GONE);
        mListView.setVisibility(View.GONE);
    }

    private void showMapView() {
        mapHeadView.setVisibility(View.VISIBLE);
        mapLayout.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.VISIBLE);
    }
    private void initView() {
        dataList = new ArrayList<>();
        mapHeadView = (RelativeLayout) findViewById(R.id.map_head_view);
        mapLayout = (RelativeLayout) findViewById(R.id.map_layout);
        mMapView = (MapView) findViewById(R.id.bmapView);
        mSendButton = (TextView) findViewById(R.id.send_btn);
        mRequestLocation = (Button) findViewById(R.id.request);
        mListView = (ListView) findViewById(R.id.lv_location_nearby);
        mSearchPoisList = (ListView) findViewById(R.id.search_pois_list);
        checkPosition = 0;
        adapter = new ListAdapter(0);
        mListView.setAdapter(adapter);
        mSearchPoisList.setAdapter(adapter);
        mSearch = GeoCoder.newInstance();

        mSearchEditView = (SearchEditView) findViewById(R.id.search_location);
        mSearchEditView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0 || "".equals(s.toString())) {
                    mSearchPoisList.setVisibility(View.GONE);
                } else {
                    //创建PoiSearch实例
                    PoiSearch poiSearch = PoiSearch.newInstance();
                    //城市内检索
                    PoiCitySearchOption poiCitySearchOption = new PoiCitySearchOption();
                    //关键字
                    poiCitySearchOption.keyword(s.toString());
                    //城市
                    poiCitySearchOption.city(city);
                    //分页编号
                    poiCitySearchOption.pageNum(0);
                    poiSearch.searchInCity(poiCitySearchOption);
                    //设置poi检索监听者
                    poiSearch.setOnGetPoiSearchResultListener(new OnGetPoiSearchResultListener() {
                        //poi 查询结果回调
                        @Override
                        public void onGetPoiResult(PoiResult poiResult) {
                            List<PoiInfo> poiInfos = poiResult.getAllPoi();
                            Log.d("allen", "onGetPoiResult");
                            PoiSearchAdapter poiSearchAdapter = new PoiSearchAdapter(mContext, poiInfos, locationLatLng);
                            mSearchPoisList.setVisibility(View.VISIBLE);
                            mSearchPoisList.setAdapter(poiSearchAdapter);
                            hideMapView();
                        }

                        //poi 详情查询结果回调
                        @Override
                        public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
                            Log.d("allen", "onGetPoiResult");
                        }

                        @Override
                        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
                            Log.d("allen", "onGetPoiResult");
                        }
                    });
                }
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();

            if (isShouldHideKeyboard(v, ev)) {
                hideKeyboard(v.getWindowToken());
            }
        }

        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d("allen", "onBackPress");
    }

    private void hideKeyboard(IBinder token) {
        if (token != null) {
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private boolean isShouldHideKeyboard(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击EditText的事件，忽略它。
                return false;
            } else {
                v.clearFocus();
                return true;
            }
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditText上，和用户用轨迹球选择其他的焦点
        return false;
    }

    /**
     * 事件初始化
     */
    private void initEvent() {
        mSearch.setOnGetGeoCodeResultListener(this);
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                checkPosition = position;
                adapter.setCheckPosition(position);
                adapter.notifyDataSetChanged();
                PoiInfo ad = (PoiInfo) adapter.getItem(position);
//                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ad.location);
//                mBaiduMap.animateMapStatus(u);
                mCurrentMarker.setPosition(ad.location);
            }
        });

        mRequestLocation.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ToastUtil.show(getApplicationContext(), "正在定位。。。");
                initLocation();
            }
        });

        mSendButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                PoiInfo poiInfo = dataList.get(checkPosition);
                ToastUtil.show(getApplicationContext(), "名称是: " + poiInfo.name + " 地址是：" + poiInfo.address
                        + "纬度是: " + poiInfo.location.latitude + "经度是: " + poiInfo.location.longitude);
            }
        });

    }

    /**
     * 定位
     */
    private void initLocation() {
        //重新设置
        checkPosition = 0;
        adapter.setCheckPosition(0);

        mBaiduMap = mMapView.getMap();
        mBaiduMap.clear();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().zoom(17).build()));   // 设置级别
        mBaiduMap.setOnMapStatusChangeListener(this);
        // 定位初始化
        mLocationClient = new LocationClient(getApplicationContext()); // 声明LocationClient类
        mLocationClient.registerLocationListener(myListener);// 注册定位监听接口

        /**
         * 设置定位参数
         */
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationMode.Hight_Accuracy);// 设置定位模式
//		option.setScanSpan(5000);// 设置发起定位请求的间隔时间,ms
        option.setNeedDeviceDirect(true);// 设置返回结果包含手机的方向
        option.setOpenGps(true);
        option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
        mLocationClient.setLocOption(option);
        mLocationClient.start(); // 调用此方法开始定位
    }


    /**
     * 定位SDK监听函数
     *
     * @author
     */
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null || mMapView == null) {
                return;
            }

            locType = location.getLocType();
            Log.i("allen", "当前定位的返回值是：" + locType);

            longitude = location.getLongitude();
            latitude = location.getLatitude();
            if (location.hasRadius()) {// 判断是否有定位精度半径
                radius = location.getRadius();
            }

            if (locType == BDLocation.TypeNetWorkLocation) {
                addrStr = location.getAddrStr();// 获取反地理编码(文字描述的地址)
                Log.i("allen", "当前定位的地址是：" + addrStr);
            }

            direction = location.getDirection();// 获取手机方向，【0~360°】,手机上面正面朝北为0°
            province = location.getProvince();// 省份
            city = location.getCity();// 城市
            district = location.getDistrict();// 区县
            locationLatLng = new LatLng(location.getLatitude(), location.getLongitude());

            //将当前位置加入List里面
            PoiInfo info = new PoiInfo();
            info.address = location.getAddrStr();
            info.city = location.getCity();
            info.location = locationLatLng;
            info.name = location.getAddrStr();
            dataList.add(info);
            adapter.notifyDataSetChanged();
            Log.i("allen", "province是：" + province + " city是" + city + " 区县是: " + district);


            // 构造定位数据
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);

//            //画标志
//            CoordinateConverter converter = new CoordinateConverter();
//            converter.coord(locationLatLng);
//            converter.from(CoordinateConverter.CoordType.COMMON);
//            LatLng convertLatLng = converter.convert();

            OverlayOptions ooA = new MarkerOptions().position(locationLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_marka));
            mCurrentMarker = (Marker) mBaiduMap.addOverlay(ooA);


//            MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(convertLatLng, 17.0f);
//            mBaiduMap.animateMapStatus(u);

            //画当前定位标志
            MapStatusUpdate uc = MapStatusUpdateFactory.newLatLng(locationLatLng);
            mBaiduMap.animateMapStatus(uc);

            mMapView.showZoomControls(false);
            //poi 搜索周边
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();
                    searchNearby();
                    Looper.loop();
                }
            }).start();

        }
    }

    /**
     * 搜索周边
     */
    private void searchNearby() {
        // POI初始化搜索模块，注册搜索事件监听
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);
        PoiNearbySearchOption poiNearbySearchOption = new PoiNearbySearchOption();

        poiNearbySearchOption.keyword("美食");
        poiNearbySearchOption.location(new LatLng(latitude, longitude));
        poiNearbySearchOption.radius(500);  // 检索半径，单位是米
        poiNearbySearchOption.pageCapacity(20);  // 默认每页10条
        mPoiSearch.searchNearby(poiNearbySearchOption);  // 发起附近检索请求
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    Log.i("----------------", "---------------------");
                    adapter.notifyDataSetChanged();
                    break;

                default:
                    break;
            }
        }
    };

    /*
     * 接受周边地理位置结果
     */
    @Override
    public void onGetPoiResult(PoiResult result) {
        // 获取POI检索结果
        if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {// 没有找到检索结果
            return;
        }

        if (result.error == SearchResult.ERRORNO.NO_ERROR) {// 检索结果正常返回
//			mBaiduMap.clear();
            if (result != null) {
                if (result.getAllPoi() != null && result.getAllPoi().size() > 0) {
                    dataList.addAll(result.getAllPoi());
//					adapter.notifyDataSetChanged();
                    Message msg = new Message();
                    msg.what = 0;
                    handler.sendMessage(msg);
                }
            }
        }
    }

    @Override
    public void onGetPoiDetailResult(PoiDetailResult result) {

    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

    }



    private class ListAdapter extends BaseAdapter {

        private int checkPosition;

        public ListAdapter(int checkPosition) {
            this.checkPosition = checkPosition;
        }

        public void setCheckPosition(int checkPosition) {
            this.checkPosition = checkPosition;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return dataList.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return dataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.baidumap_list_item, null);

                holder.textView = (TextView) convertView.findViewById(R.id.text_name);
                holder.textAddress = (TextView) convertView.findViewById(R.id.text_address);
                holder.imageLl = (ImageView) convertView.findViewById(R.id.image);
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Log.i("allen", "name地址是：" + dataList.get(position).name);
            Log.i("allen", "address地址是：" + dataList.get(position).address);

            holder.textView.setText(dataList.get(position).name);
            holder.textAddress.setText(dataList.get(position).address);
            if (checkPosition == position) {
                holder.imageLl.setVisibility(View.VISIBLE);
            } else {
                holder.imageLl.setVisibility(View.GONE);
            }


            return convertView;
        }

    }

    class ViewHolder {
        TextView textView;
        TextView textAddress;
        ImageView imageLl;
    }

    private class PoiSearchAdapter extends BaseAdapter {

        private Context context;
        private List<PoiInfo> poiInfos;
        private final LatLng locationLatLng;

        public PoiSearchAdapter(Context context, List<PoiInfo> poiInfos, LatLng locationLatLng) {
            this.context = context;
            this.poiInfos = poiInfos;
            this.locationLatLng = locationLatLng;
        }

        @Override
        public int getCount() {
            return poiInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return poiInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.poisearch_item, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            PoiInfo poiInfo = poiInfos.get(position);
            holder.mPoisearchName.setText(poiInfo.name);
            holder.mPoisearchAddress.setText(poiInfo.address);
            return convertView;
        }

        class ViewHolder {
            TextView mPoisearchName;
            TextView mPoisearchAddress;

            public ViewHolder(View view) {
                mPoisearchName = (TextView) view.findViewById(R.id.poisearch_name);
                mPoisearchAddress = (TextView) view.findViewById(R.id.poisearch_address);
            }
        }
    }

    @Override
    protected void onDestroy() {
        // 退出时销毁定位
        if (mLocationClient != null) {
            mLocationClient.stop();
        }

        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mPoiSearch.destroy();
        super.onDestroy();
        // 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();

        // 取消监听 SDK 广播
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

}