package com.xxxifan.devbox.library.helpers;

import android.os.Build;
import android.support.annotation.ColorInt;
import android.view.WindowManager;

import com.xxxifan.devbox.library.AppDelegate;
import com.xxxifan.devbox.library.R;
import com.xxxifan.devbox.library.ui.BaseActivity;

import java.lang.ref.WeakReference;

/**
 * Created by xifan on 15-7-16.
 */
public class ActivityConfig {

    private WeakReference<BaseActivity> mActivity;

    @ColorInt
    private int mToolbarColor;
    private boolean mUseToolbar;
    private boolean mIsLinearRoot;

    private ActivityConfig(BaseActivity activity) {
        mActivity = new WeakReference<>(activity);
    }

    public static ActivityConfig newInstance(BaseActivity activity) {
        ActivityConfig config = new ActivityConfig(activity);
        config.setToolbarColor(AppDelegate.get().getResources().getColor(R.color.colorPrimary));
        config.setUseToolbar(true);
        config.setIsLinearRoot(true);
        config.setTranslucentStatusBar(true);
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

    public ActivityConfig setTranslucentStatusBar(boolean enable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && mActivity != null) {
            if (enable) {
                mActivity.get().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            } else {
                mActivity.get().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
        }
        return this;
    }

    public ActivityConfig setTranslucentNavBar(boolean enable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && mActivity != null) {
            if (enable) {
                mActivity.get().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            } else {
                mActivity.get().getWindow().clearFlags(WindowManager.LayoutParams
                        .FLAG_TRANSLUCENT_NAVIGATION);
            }
        }
        return this;
    }
}
