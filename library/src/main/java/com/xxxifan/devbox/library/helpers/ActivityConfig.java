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

    public void setToolbarColor(int toolbarColor) {
        mToolbarColor = toolbarColor;
    }

    public boolean useToolbar() {
        return mUseToolbar;
    }

    public void setUseToolbar(boolean useToolbar) {
        mUseToolbar = useToolbar;
    }

    public boolean isLinearRoot() {
        return mIsLinearRoot;
    }

    public void setIsLinearRoot(boolean isLinearRoot) {
        mIsLinearRoot = isLinearRoot;
    }

    public class Builder {
        private ActivityConfig mConfig;

        public Builder() {
            mConfig = ActivityConfig.newInstance();
        }

        public ActivityConfig useToolbar(boolean useToolbar) {
            mUseToolbar = useToolbar;
            return mConfig;
        }

        public ActivityConfig toolbarColor(@ColorInt int color) {
            mToolbarColor = color;
            return mConfig;
        }

        public ActivityConfig linearRoot(boolean isLinearRoot) {
            mIsLinearRoot = isLinearRoot;
            return mConfig;
        }

        public ActivityConfig build() {
            if (mConfig == null) {
                mConfig = new ActivityConfig();
            }
            return mConfig;
        }
    }
}
