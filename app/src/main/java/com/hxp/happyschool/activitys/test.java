package com.hxp.happyschool.activitys;
import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.hxp.happyschool.R;
import java.util.List;


/**
 * Created by hxp on 15-11-28.
 */
public class test extends Activity {
    private List<ScanResult> resultList;
    private WifiManager obj_wifi;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_layout);
        init();
    }
    public class myAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return resultList.size();
        }
        @Override
        public Object getItem(int arg0) {
            return arg0;
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater flater = LayoutInflater.from(test.this);
            convertView = flater.inflate(R.layout.item,null);
            ScanResult mScanResult = resultList.get(position);
            TextView ssid = (TextView) convertView.findViewById(R.id.textView);
            ssid.setText(mScanResult.SSID);
            TextView singal = (TextView) convertView.findViewById(R.id.signal_strenth);
            singal.setText(String.valueOf(Math.abs(mScanResult.level)));
            return convertView;
        }
    }
    private void init() {
        obj_wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        resultList = obj_wifi.getScanResults();
        ListView list_result = (ListView) findViewById(R.id.list_wifi);
        list_result.setAdapter(new myAdapter());
    }
}
