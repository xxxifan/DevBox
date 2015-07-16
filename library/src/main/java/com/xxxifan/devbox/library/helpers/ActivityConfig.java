package com.xxxifan.devbox.library.helpers;

import android.support.annotation.ColorInt;

import com.xxxifan.devbox.library.AppDelegate;
import com.xxxifan.devbox.library.R;

/**
 * Created by xifan on 15-7-16.
 */
public class ActivityConfig {

    @ColorInt
    private int mToolbarColor;
    private boolean mUseToolbar;
    private boolean mIsLinearRoot;

    private ActivityConfig() {
    }

    public static ActivityConfig newInstance() {
        ActivityConfig config = new ActivityConfig();
        config.setToolbarColor(AppDelegate.get().getResources().getColor(R.color.colorPrimary));
        config.setUseToolbar(true);
        config.setIsLinearRoot(true);
        return config;
    }

    public int getToolbarColor() {
        return mToolbarColor;
    }

    public ActivityConfig setToolbarColor(int toolbarColor) {
        mToolbarColor = toolbarColor;
        return this;
    }

    public boolean useToolbar() {
        return mUseToolbar;
    }

    public ActivityConfig setUseToolbar(boolean useToolbar) {
        mUseToolbar = useToolbar;
        return this;
    }

    public boolean isLinearRoot() {
        return mIsLinearRoot;
    }

    public ActivityConfig setIsLinearRoot(boolean isLinearRoot) {
        mIsLinearRoot = isLinearRoot;
        return this;
    }
}
