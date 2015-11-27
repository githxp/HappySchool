package com.hxp.happyschool.activitys;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MyTrafficStyle;
import com.amap.api.maps.model.NaviPara;
import com.amap.api.maps.overlay.PoiOverlay;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.hxp.happyschool.R;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.hxp.happyschool.utils.AMapUtil;
import com.hxp.happyschool.utils.ToastUtil;
import android.content.pm.PackageManager.NameNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hxp on 15-11-26.
 */
public class Map3DActivity extends Activity implements OnClickListener, LocationSource,
        AMapLocationListener,OnMarkerClickListener, InfoWindowAdapter, TextWatcher,
        OnPoiSearchListener{
    private MapView mapView;
    private AMap aMap;
    private OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private String obj_keyword = "";// 要输入的poi搜索关键字
    private ProgressDialog obj_progdialog = null;// 搜索时进度条
    private PoiResult obj_poiresult; // poi返回的结果
    private int obj_currentpage = 0;// 当前页面，从0开始计数
    private PoiSearch.Query obj_query;// Poi查询条件类
    private PoiSearch obj_poisearch;// POI搜索
    private EditText obj_editcity;
    private AutoCompleteTextView obj_editsearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_3dmap);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        init();
        setUpMap(); //启动定位方法
    }

    //初始化AMap对象
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
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

    //设置setUpMap方法
    private void setUpMap() {
        aMap.setLocationSource(this); //设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true); //设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true); //设置为true表示显示定位层并可触发定位,false表示隐藏定位层并不可触发定位,默认是false
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
        Button obj_poisearch = (Button) findViewById(R.id.btn_poisearch);
        obj_poisearch.setOnClickListener(this);
        Button obj_nextresult = (Button) findViewById(R.id.btn_nextresult);
        obj_nextresult.setOnClickListener(this);
        obj_editsearch = (AutoCompleteTextView) findViewById(R.id.edit_poisearch);
        obj_editsearch.addTextChangedListener(this);// 添加文本输入框监听事件
        obj_editcity = (EditText) findViewById(R.id.edit_poicity);
        aMap.setOnMarkerClickListener(this);// 添加点击marker监听事件
        aMap.setInfoWindowAdapter(this);// 添加显示infowindow监听事件
    }

    /**
     * 点击搜索按钮
     */
    public void mth_search() {
        obj_keyword = AMapUtil.checkEditText(obj_editcity);
        if ("".equals(obj_keyword)) {
            ToastUtil.show(Map3DActivity.this, "请输入搜索关键字");
            return;
        } else {
            doSearchQuery();
        }
    }

    /**
     * 点击下一页按钮
     */
    public void mth_nextresult() {
        if (obj_query != null && obj_poisearch != null && obj_poiresult != null) {
            if (obj_poiresult.getPageCount() - 1 > obj_currentpage) {
                obj_currentpage++;
                obj_query.setPageNum(obj_currentpage);// 设置查后一页
                obj_poisearch.searchPOIAsyn();
            } else {
                ToastUtil.show(Map3DActivity.this,
                        R.string.txt_noresult);
            }
        }
    }

    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if (obj_progdialog == null)
            obj_progdialog = new ProgressDialog(this);
        obj_progdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        obj_progdialog.setIndeterminate(false);
        obj_progdialog.setCancelable(false);
        obj_progdialog.setMessage("正在搜索:\n" + obj_keyword);
        obj_progdialog.show();
    }

    /**
     * 隐藏进度框
     */
    private void dissmissProgressDialog() {
        if (obj_progdialog != null) {
            obj_progdialog.dismiss();
        }
    }

    /**
     * 开始进行poi搜索
     */
    protected void doSearchQuery() {
        showProgressDialog();// 显示进度框
        obj_currentpage = 0;
        obj_query = new PoiSearch.Query(obj_keyword, "",obj_editcity.getText().toString());// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        obj_query.setPageSize(10);// 设置每页最多返回多少条poiitem
        obj_query.setPageNum(obj_currentpage);// 设置查第一页

        obj_poisearch = new PoiSearch(this, obj_query);
        obj_poisearch.setOnPoiSearchListener(this);
        obj_poisearch.searchPOIAsyn();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return false;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public View getInfoWindow(final Marker marker) {
        View view = getLayoutInflater().inflate(R.layout.poikeywordsearch_uri,
                null);
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(marker.getTitle());

        TextView snippet = (TextView) view.findViewById(R.id.snippet);
        snippet.setText(marker.getSnippet());
        ImageButton button = (ImageButton) view
                .findViewById(R.id.start_amap_app);
        // 调起高德地图app
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAMapNavi(marker);
            }
        });
        return view;
    }
    /**
     * 调起高德地图导航功能，如果没安装高德地图，会进入异常，可以在异常中处理，调起高德地图app的下载页面
     */
    public void startAMapNavi(Marker marker) {
        // 构造导航参数
        NaviPara naviPara = new NaviPara();
        // 设置终点位置
        naviPara.setTargetPoint(marker.getPosition());
        // 设置导航策略，这里是避免拥堵
        naviPara.setNaviStyle(NaviPara.DRIVING_AVOID_CONGESTION);

        // 调起高德地图导航
        try {
            AMapUtils.openAMapNavi(naviPara, getApplicationContext());
        } catch (com.amap.api.maps.AMapException e) {

            // 如果没安装会进入异常，调起下载页面
            AMapUtils.getLatestAMapApp(getApplicationContext());

        }

    }

    /**
     * 判断高德地图app是否已经安装
     */
    public boolean getAppIn() {
        PackageInfo packageInfo = null;
        try {
            packageInfo = this.getPackageManager().getPackageInfo(
                    "com.autonavi.minimap", 0);
        } catch (NameNotFoundException e) {
            packageInfo = null;
            e.printStackTrace();
        }
        // 本手机没有安装高德地图app
        if (packageInfo != null) {
            return true;
        }
        // 本手机成功安装有高德地图app
        else {
            return false;
        }
    }

    /**
     * 获取当前app的应用名字
     */
    public String getApplicationName() {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        try {
            packageManager = getApplicationContext().getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(
                    getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        String applicationName = (String) packageManager
                .getApplicationLabel(applicationInfo);
        return applicationName;
    }

    /**
     * poi没有搜索到数据，返回一些推荐城市的信息
     */
    private void showSuggestCity(List<SuggestionCity> cities) {
        String infomation = "推荐城市\n";
        for (int i = 0; i < cities.size(); i++) {
            infomation += "城市名称:" + cities.get(i).getCityName() + "城市区号:"
                    + cities.get(i).getCityCode() + "城市编码:"
                    + cities.get(i).getAdCode() + "\n";
        }
        ToastUtil.show(Map3DActivity.this, infomation);

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String newText = s.toString().trim();
        Inputtips inputTips = new Inputtips(Map3DActivity.this,
                new Inputtips.InputtipsListener() {

                    @Override
                    public void onGetInputtips(List<Tip> tipList, int rCode) {
                        if (rCode == 0) {// 正确返回
                            List<String> listString = new ArrayList<String>();
                            for (int i = 0; i < tipList.size(); i++) {
                                listString.add(tipList.get(i).getName());
                            }
                            ArrayAdapter<String> aAdapter = new ArrayAdapter<String>(
                                    getApplicationContext(),
                                    R.layout.route_inputs, listString);
                            obj_editsearch.setAdapter(aAdapter);
                            aAdapter.notifyDataSetChanged();
                        }
                    }
                });
        try {
            inputTips.requestInputtips(newText, obj_editcity.getText().toString());// 第一个参数表示提示关键字，第二个参数默认代表全国，也可以为城市区号

        } catch (AMapException e) {
            e.printStackTrace();
        }
    }


    /**
     * POI信息查询回调方法
     */
    @Override
    public void onPoiSearched(PoiResult result, int rCode) {
        dissmissProgressDialog();// 隐藏对话框
        if (rCode == 0) {
            if (result != null && result.getQuery() != null) {// 搜索poi的结果
                if (result.getQuery().equals(obj_query)) {// 是否是同一条
                    obj_poiresult = result;
                    // 取得搜索到的poiitems有多少页
                    List<PoiItem> poiItems = obj_poiresult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                    List<SuggestionCity> suggestionCities = obj_poiresult
                            .getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息

                    if (poiItems != null && poiItems.size() > 0) {
                        aMap.clear();// 清理之前的图标
                        PoiOverlay poiOverlay = new PoiOverlay(aMap, poiItems);
                        poiOverlay.removeFromMap();
                        poiOverlay.addToMap();
                        poiOverlay.zoomToSpan();
                    } else if (suggestionCities != null
                            && suggestionCities.size() > 0) {
                        showSuggestCity(suggestionCities);
                    } else {
                        ToastUtil.show(Map3DActivity.this,
                                R.string.txt_noresult);
                    }
                }
            } else {
                ToastUtil.show(Map3DActivity.this,
                        R.string.txt_noresult);
            }
        } else if (rCode == 27) {
            ToastUtil.show(Map3DActivity.this,
                    R.string.txt_errornetwork);
        } else if (rCode == 32) {
            ToastUtil.show(Map3DActivity.this, R.string.txt_errorkey);
        } else {
            ToastUtil.show(Map3DActivity.this,
                    getString(R.string.txt_errorother) + rCode);
        }

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
        switch (v.getId()) {
            /**
             * 点击搜索按钮
             */
            case R.id.btn_poisearch:
                mth_search();
                break;
            /**
             * 点击下一页按钮
             */
            case R.id.btn_nextresult:
                mth_nextresult();
                break;
            default:
                break;
        }
    }
}