package com.ashlikun.circleprogress.simple;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;

import com.ashlikun.circleprogress.CircleProgressView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final CircleProgressView view = findViewById(R.id.progressView);
        view.setColor(0xff0000);
        postDelay();
    }

    private void postDelay() {
        final CircleProgressView view = findViewById(R.id.progressView);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!view.isRunning()) {
                    view.start();
                } else {
                    view.stop();
                }
                postDelay();
            }
        }, 100);
    }


}
