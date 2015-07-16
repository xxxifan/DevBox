package com.xxxifan.devbox.demo;

import android.app.Application;

import com.xxxifan.devbox.library.AppDelegate;

/**
 * Created by xifan on 15-7-16.
 */
public class App extends Application {
    private static App sInstance;

    public static App getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        // init module
        AppDelegate.install(this);
    }
}
