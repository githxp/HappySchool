package com.hxp.happyschool.activitys;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.MyTrafficStyle;
import com.hxp.happyschool.R;

/**
 * Created by hxp on 15-11-26.
 */
public class Map3DActivity extends Activity implements OnClickListener, LocationSource, AMapLocationListener {
    private MapView mapView;
    private AMap aMap;
    private OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_3dmap);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        init();
    }

    //初始化AMap对象
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
            setUpMap(); //启动定位方法
        }
        //实例化交通图对象
        CheckBox obj_traffic = (CheckBox) findViewById(R.id.check_traffic);
        obj_traffic.setOnClickListener(this);
        //添加卫星选择框单击事件
        final CheckBox obj_satellite = (CheckBox) findViewById(R.id.check_satellite);
        obj_satellite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (obj_satellite.isChecked()) {
                    aMap.setMapType(AMap.MAP_TYPE_SATELLITE); //显示卫星地图模式
                } else {
                    aMap.setMapType(AMap.MAP_TYPE_NORMAL); //显示矢量地图模式
                }
            }
        });
        //添加夜景选择框单击事件
        final CheckBox obj_night = (CheckBox) findViewById(R.id.check_night);
        obj_night.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (obj_night.isChecked()) {
                    aMap.setMapType(AMap.MAP_TYPE_NIGHT); //显示夜景地图模式
                } else {
                    aMap.setMapType(AMap.MAP_TYPE_NORMAL); //显示矢量地图模式
                }
            }
        });
        //实例化交通图层样式对象并设置样式
        MyTrafficStyle myTrafficStyle = new MyTrafficStyle();
        myTrafficStyle.setSeriousCongestedColor(0xff92000a);
        myTrafficStyle.setCongestedColor(0xffea0312);
        myTrafficStyle.setSlowColor(0xffff7508);
        myTrafficStyle.setSmoothColor(0xff00a209);
        aMap.setMyTrafficStyle(myTrafficStyle);
    }

    //设置一setUpMap方法
    private void setUpMap() {
        aMap.setLocationSource(this); //设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true); //设置为true表示显示定位层并可触发定位,false表示隐藏定位层并不可触发定位,默认是false
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation != null
                    && amapLocation.getErrorCode() == 0) {
                mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
            }
            else {
                String errText = "定位失败" + amapLocation.getErrorCode()+ ": " + amapLocation.getErrorInfo();
                Toast.makeText(Map3DActivity.this,errText,Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
        }
    }


    //停止定位
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }


    //方法必须重写
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }


    //方法必须重写
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    //方法必须重写
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    //方法必须重写
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    //对选择是否显示交通状况事件的响应
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.check_traffic) {
            aMap.setTrafficEnabled(((CheckBox) v).isChecked()); //显示实时交通状况
        }
    }
}