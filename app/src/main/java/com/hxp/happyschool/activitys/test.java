package com.hxp.happyschool.activitys;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.hxp.happyschool.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListResourceBundle;

/**
 * Created by hxp on 15-11-28.
 */
public class test extends Activity {
    private TextView txt_wifi;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_layout);
        init();
    }
    private void init(){
        WifiManager obj_wifi =(WifiManager) getSystemService(Context.WIFI_SERVICE);
        obj_wifi.setWifiEnabled(true);
        WifiInfo wifiinfo = obj_wifi.getConnectionInfo();
        int rssi = wifiinfo.getRssi();
        int ca = obj_wifi.calculateSignalLevel(rssi, 15);
        obj_wifi.startScan();
        obj_wifi.getScanResults();
        ListView list_result = (ListView) findViewById(R.id.list_wifi);
        List<ScanResult> resultList = obj_wifi.getScanResults();
        String mac = wifiinfo.getMacAddress();
        String ip = intToIp(wifiinfo.getIpAddress());
        String status = "";
        if (obj_wifi.getWifiState() == WifiManager.WIFI_STATE_ENABLED){
            status = "wifi已经打开了";
        }

        ArrayList<HashMap<String,Object>> list=new ArrayList<HashMap<String,Object>>();
        SimpleAdapter adapter=new SimpleAdapter(test.this,
                list,android.R.layout.simple_list_item_1, new String[]{"scoreId"},
                new int[]{R.id.txt_result});

        String ssid = wifiinfo.getSSID();
        int networkid = wifiinfo.getNetworkId();
        int speed = wifiinfo.getLinkSpeed();
        txt_wifi = (TextView) findViewById(R.id.txt_wifiinfo);
        txt_wifi.setText("MAC地址是:"+mac+"\n\r"+"IP地址是:"+ip+"\n\r"+"wifi状态是:"+status+"\n\r"+"SSID是:"+ssid+
                "\n\r"+"网络id是:"+networkid+"\n\r"+"网络连接速度是:"+speed+"\n\r"+"rssi:"+rssi+"\n\r"+
        "信号强度是:"+ca+"\n\r");

    }
    private String intToIp(int ip){
        return (ip & 0xFF)+ "." +((ip>>8) & 0xFF)+ "." +((ip >>16) &0xFF)+ "." +((ip >>24) & 0XFF);
    }

}
