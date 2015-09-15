package com.xxxifan.devbox.demo;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.xxxifan.devbox.library.helpers.ActivityConfig;
import com.xxxifan.devbox.library.tools.HttpUtils;
import com.xxxifan.devbox.library.ui.BaseActivity;

import java.io.IOException;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreateActivityConfig(ActivityConfig config) {
        config.setShowHomeAsUpKey(false);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView(View rootView) {

    }

    public void onTestClick(View view) {
        HttpUtils.get("http://api.funwoo.net/users/23/get/helpee/information/", new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                if (e != null) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onResponse(Response response) throws IOException {
                Log.e("", response.body().string());
            }
        });
    }
}
