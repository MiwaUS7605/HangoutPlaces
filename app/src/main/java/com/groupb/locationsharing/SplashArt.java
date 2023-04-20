package com.groupb.locationsharing;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class SplashArt extends AppCompatActivity {
    Thread timer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_art);
        timer = new Thread(){
            @Override
            public void run() {
                try {
                    synchronized (this){
                        wait(2000);
                    }
                } catch (InterruptedException e){
                    e.printStackTrace();
                } finally {
                    Intent intent = new Intent(SplashArt.this, StartActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
        timer.start();
    }
}