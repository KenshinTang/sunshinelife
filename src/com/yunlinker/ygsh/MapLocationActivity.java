package com.yunlinker.ygsh;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;
import com.plugin.myPlugin.bean.LocationBean;
import com.yunlinker.ygsh.view.SearchEditView;

import java.util.ArrayList;
import java.util.List;


/**
 * author:  Allen <br>
 * date:  2017/8/19 16:33<br>
 * description:
 */
public class MapLocationActivity extends Activity implements OnGetPoiSearchResultListener, BaiduMap.OnMapStatusChangeListener {
    /**
     * 定位SDK的核心类
     */
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();
    private Context mContext;
    private MapView mMapView = null;
    private BaiduMap mBaiduMap;
    private TextView mSendButton;
    private Button mRequestLocation;
    private TextView mCancel;
    private ListView mSearchResultList;
    // 搜索周边相关
    private PoiSearch mPoiSearch = null;
    private ListAdapter mListAdapter;
    private float radius;// 定位精度半径，单位是米
    private String addrStr;// 反地理编码
    private String city;// 城市信息
    private ListView mSearchPoisList;
    private RelativeLayout mapHeadView;
    private RelativeLayout mapLayout;
    private SDKReceiver mReceiver;
    private int checkPosition;
    //搜索editText
    private SearchEditView mSearchEditView;

    @Override
    public void onMapStatusChangeStart(MapStatus mapStatus) {
        Logger.d("onMapStatusChangeStart");
    }

    @Override
    public void onMapStatusChange(MapStatus mapStatus) {
        Logger.d("onMapStatusChange");
    }

    //移动地图对附近做搜索
    @Override
    public void onMapStatusChangeFinish(MapStatus mapStatus) {
        Logger.d("onMapStatusChangeFinish");
        searchMoveFinish(mapStatus.target);
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
        initLocation();
    }

    private void hideMapView() {
        mapHeadView.setVisibility(View.GONE);
        mapLayout.setVisibility(View.GONE);
        mSearchResultList.setVisibility(View.GONE);
    }

    private void showMapView() {
        mapHeadView.setVisibility(View.VISIBLE);
        mapLayout.setVisibility(View.VISIBLE);
        mSearchResultList.setVisibility(View.VISIBLE);
        mSearchEditView.setText("");
    }

    private void initView() {
        mapHeadView = (RelativeLayout) findViewById(R.id.map_head_view);
        mapLayout = (RelativeLayout) findViewById(R.id.map_layout);
        mMapView = (MapView) findViewById(R.id.bmapView);
        mSendButton = (TextView) findViewById(R.id.send_btn);
        mRequestLocation = (Button) findViewById(R.id.request);
        mCancel = (TextView) findViewById(R.id.cancel_btn);
        mSearchResultList = (ListView) findViewById(R.id.lv_location_nearby);
        mSearchPoisList = (ListView) findViewById(R.id.search_pois_list);
        mSearchPoisList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                showMapView();
                PoiInfo item = (PoiInfo) mSearchPoisList.getAdapter().getItem(position);
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(item.location);
                mBaiduMap.setMapStatus(u);
                searchMoveFinish(item.location);
                Logger.d("onItemClick: poiInfo" + item.name + " " + item.address + " " + item.location.toString());
            }
        });
        mListAdapter = new ListAdapter(0);
        mSearchResultList.setAdapter(mListAdapter);

        mSearchEditView = (SearchEditView) findViewById(R.id.search_location);
        mSearchEditView.setOnFinishComposingListener(new SearchEditView.OnFinishComposingListener() {
            @Override
            public void finishComposing() {
                showMapView();
                mSearchEditView.clearFocus();
                Logger.d("finishComposing: ");
            }
        });
        mSearchEditView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0 || "".equals(s.toString()) || TextUtils.isEmpty(city)) {
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
                    //设置poi检索监听者
                    poiSearch.setOnGetPoiSearchResultListener(new OnGetPoiSearchResultListener() {
                        //poi 查询结果回调
                        @Override
                        public void onGetPoiResult(PoiResult poiResult) {
                            if (poiResult == null) {
                                return;
                            }
                            List<PoiInfo> poiInfos = poiResult.getAllPoi();
                            Logger.d("onGetPoiResult");
                            PoiSearchAdapter poiSearchAdapter = new PoiSearchAdapter(mContext, poiInfos);
                            mSearchPoisList.setVisibility(View.VISIBLE);
                            mSearchPoisList.setAdapter(poiSearchAdapter);
                            hideMapView();
                        }

                        //poi 详情查询结果回调
                        @Override
                        public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
                            Logger.d("onGetPoiResult");
                        }

                        @Override
                        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
                            Logger.d("onGetPoiResult");
                        }
                    });
                    poiSearch.searchInCity(poiCitySearchOption);
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
                if (mSearchPoisList.getVisibility() == View.VISIBLE) {
                    return false;
                }
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
        mSearchResultList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                checkPosition = position;
                mListAdapter.setCheckPosition(position);
                mListAdapter.notifyDataSetChanged();
                PoiInfo ad = (PoiInfo) mListAdapter.getItem(position);
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ad.location);
                mBaiduMap.setMapStatus(u);
            }
        });
        mCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mRequestLocation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                initLocation();
            }
        });
        mSendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Object item = mListAdapter.getItem(checkPosition);
                if (item == null) {
                    setResult(0);
                    return;
                }
                PoiInfo poiInfo = (PoiInfo) item;
                LocationBean locationBean = new LocationBean();
                locationBean.setCode(1);
                locationBean.setMsg("定位成功");
                locationBean.setLocation_name(poiInfo.name);
                locationBean.setLocation_adress(poiInfo.address);
                locationBean.setLocation_lat(poiInfo.location.latitude);
                locationBean.setLocation_lng(poiInfo.location.longitude);
                locationBean.setCity(poiInfo.city);
                locationBean.setAddr(poiInfo.address);
                Intent intent = new Intent();
                Gson gson = new Gson();
                String jsonStr = gson.toJson(locationBean);
                intent.putExtra("data", jsonStr);
                setResult(1, intent);
                finish();
            }
        });
    }

    /**
     * 定位
     */
    private void initLocation() {
        //重新设置
        checkPosition = 0;
        mListAdapter.setCheckPosition(0);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.clear();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().zoom(17).build()));   // 设置级别
        mBaiduMap.setOnMapStatusChangeListener(this);
        // 定位初始化
        mLocationClient = new LocationClient(getApplicationContext()); // 声明LocationClient类
        mLocationClient.registerLocationListener(myListener);// 注册定位监听接口
        //设置定位参数
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationMode.Hight_Accuracy);// 设置定位模式
        option.setCoorType("bd09ll");
        option.setNeedDeviceDirect(true);// 设置返回结果包含手机的方向
        option.setOpenGps(true);
        option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
        mLocationClient.setLocOption(option);
        mLocationClient.start(); // 调用此方法开始定位
    }

    private void searchMoveFinish(LatLng target) {
        GeoCoder geoCoder = GeoCoder.newInstance();
        ReverseGeoCodeOption reverseCoder = new ReverseGeoCodeOption();
        reverseCoder.location(target);
        Logger.d("searchMoveFinish: ");
        geoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult arg0) {
                if (arg0 != null && arg0.getPoiList() != null) {
                    mListAdapter.setData(arg0.getPoiList());
                    mSearchResultList.setAdapter(mListAdapter);
                    mListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onGetGeoCodeResult(GeoCodeResult arg0) {
            }
        });
        geoCoder.reverseGeoCode(reverseCoder);
    }

    /*
     * 接受周边地理位置结果
     */
    @Override
    public void onGetPoiResult(PoiResult poiResult) {
        if (poiResult != null && poiResult.getAllPoi() != null) {
            mListAdapter.setData(poiResult.getAllPoi());
            mListAdapter.notifyDataSetChanged();
        }
        mListAdapter.setCheckPosition(0);
    }

    @Override
    public void onGetPoiDetailResult(PoiDetailResult result) {

    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

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

    /**
     * 构造广播监听类，监听 SDK key 验证以及网络异常广播
     */
    public class SDKReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String s = intent.getAction();
            if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
                Logger.d("key 验证出错! 错误码 :" + intent.getIntExtra
                        (SDKInitializer.SDK_BROADTCAST_INTENT_EXTRA_INFO_KEY_ERROR_CODE, 0)
                        + " ; 请在 AndroidManifest.xml 文件中检查 key 设置");
            } else if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK)) {
                Logger.d("key 验证成功! 功能可以正常使用");
            } else if (s.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
                Logger.d("网络出错");
            }
        }
    }

    /**
     * 定位SDK监听函数
     *
     * @author
     */
    private class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null || mMapView == null) {
                return;
            }
            int locType = location.getLocType();
            if (location.hasRadius()) {// 判断是否有定位精度半径
                radius = location.getRadius();
            }
            if (locType == BDLocation.TypeNetWorkLocation) {
                addrStr = location.getAddrStr();// 获取反地理编码(文字描述的地址)
            }
            city = location.getCity();// 城市
            LatLng locationLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            // 构造定位数据
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(radius)
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            mMapView.showZoomControls(false);
            // 创建POI检索实例
            mPoiSearch = PoiSearch.newInstance();
            // 设置监听器
            mPoiSearch.setOnGetPoiSearchResultListener(MapLocationActivity.this);
            //设置地图中心点位置
            MapStatus status = new MapStatus.Builder().target(locationLatLng).build();
            searchMoveFinish(status.target);
            MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(locationLatLng, 17.0f);
            mBaiduMap.animateMapStatus(u);
        }
    }

    private class ListAdapter extends BaseAdapter {
        private List<PoiInfo> dataList = new ArrayList<>();
        private int checkPosition;

        public ListAdapter(int checkPosition) {
            this.checkPosition = checkPosition;
        }

        public void setCheckPosition(int checkPosition) {
            this.checkPosition = checkPosition;
        }

        public void setData(List<PoiInfo> dataList) {
            this.dataList = dataList;
        }

        @Override
        public int getCount() {
            if (dataList == null) {
                return 0;
            }
            return dataList.size();
        }

        @Override
        public Object getItem(int position) {
            if (dataList == null || position >= dataList.size()) {
                return null;
            }
            return dataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
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
        private List<PoiInfo> poiInfos = new ArrayList<>();

        public PoiSearchAdapter(Context context, List<PoiInfo> poiInfos) {
            this.context = context;
            this.poiInfos = poiInfos;
        }

        @Override
        public int getCount() {
            if (poiInfos == null) {
                return 0;
            }
            return poiInfos.size();
        }

        @Override
        public Object getItem(int position) {
            if (poiInfos == null || position >= poiInfos.size()) {
                return null;
            }
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

}