package com.hxp.happyschool.activitys;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.hxp.happyschool.R;

/**
 * Created by hxp on 15-11-29.
 */
public class thread extends Activity {
    private Thread mThread;
    private static MediaPlayer mp = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.thread);
        Button btn = (Button) findViewById(R.id.btn_play);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                mThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        playBGsound();
                    }
                });
                mThread.start();
            }
        });
    }
    private void playBGsound(){
        if(mp != null){
            mp.release();
        }
        mp = MediaPlayer.create(thread.this,R.raw.a1);
        mp.start();
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                try {
                    mThread.sleep(5000);
                    playBGsound();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

}
