package com.xxxifan.devbox.demo;

import android.graphics.Color;
import android.os.Bundle;

import com.xxxifan.devbox.library.ui.BaseActivity;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getActivityConfig()
                .setToolbarColor(Color.TRANSPARENT)
                .setIsLinearRoot(false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void initView() {
    }
}
