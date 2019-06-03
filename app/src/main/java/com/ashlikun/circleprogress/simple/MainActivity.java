package com.ashlikun.circleprogress.simple;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ashlikun.circleprogress.CircleProgressView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CircleProgressView view = findViewById(R.id.progressView);
        view.setColor(0xff0000);
    }


}
