package com.hxp.happyschool.activitys;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.hxp.happyschool.R;

/**
 * Created by hxp on 15-11-26.
 */
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        Button obj_navigation = (Button) findViewById(R.id.btn_navigation);
        obj_navigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent6 = new Intent(MainActivity.this,Map3DActivity.class);
                startActivity(intent6);
            }
        });
}
}
