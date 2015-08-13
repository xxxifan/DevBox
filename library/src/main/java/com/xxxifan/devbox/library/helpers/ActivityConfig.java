package com.xxxifan.devbox.library.helpers;

import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.view.WindowManager;

import com.xxxifan.devbox.library.AppDelegate;
import com.xxxifan.devbox.library.R;
import com.xxxifan.devbox.library.tools.Log;
import com.xxxifan.devbox.library.ui.BaseActivity;

import java.lang.ref.WeakReference;

/**
 * Visual configs for BaseActivity, set all customizations here.
 */
public class ActivityConfig {

    private WeakReference<BaseActivity> mActivity;

    @ColorInt
    private int mToolbarColor;
    @LayoutRes
    private int mDrawerHeaderResId;
    private int mDrawerIconId;
    private int mDrawerMenuId;
    private boolean mUseToolbar;
    private boolean mIsLinearRoot;
    private boolean mShowHomeAsUpKey;
    private boolean mIsFitSystemWindow;
    private boolean mIsDrawerLayout;

    private ActivityConfig(BaseActivity activity) {
        mActivity = new WeakReference<>(activity);
    }

    public static ActivityConfig newInstance(BaseActivity activity) {
        ActivityConfig config = new ActivityConfig(activity);
        config.setToolbarColor(AppDelegate.get().getResources().getColor(R.color.colorPrimary));
        config.setUseToolbar(true);
        config.setShowHomeAsUpKey(true);
        config.setIsLinearRoot(true);
        config.setTranslucentStatusBar(true);
        config.setFitSystemWindow(false);
        return config;
    }

    public int getToolbarColor() {
        return mToolbarColor;
    }

    public ActivityConfig setToolbarColor(@ColorInt int toolbarColor) {
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

    public boolean showHomeAsUpKey() {
        return mShowHomeAsUpKey;
    }

    public ActivityConfig setShowHomeAsUpKey(boolean enable) {
        mShowHomeAsUpKey = enable;
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && mActivity != null && mActivity.get()
                != null) {
            if (enable) {
                mActivity.get().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            } else {
                mActivity.get().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
        } else if (mActivity == null || mActivity.get() == null) {
            Log.e(this, "Activity is null! translucent statusbar is not set");
        }
        return this;
    }

    public ActivityConfig setTranslucentNavBar(boolean enable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && mActivity != null && mActivity.get()
                != null) {
            if (enable) {
                mActivity.get().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            } else {
                mActivity.get().getWindow().clearFlags(WindowManager.LayoutParams
                        .FLAG_TRANSLUCENT_NAVIGATION);
            }
        } else if (mActivity == null || mActivity.get() == null) {
            Log.e(this, "Activity is null! translucent navbar is not set");
        }
        return this;
    }

    public boolean isFitSystemWindow() {
        return mIsFitSystemWindow;
    }

    public ActivityConfig setFitSystemWindow(boolean value) {
        mIsFitSystemWindow = value;
        return this;
    }

    public ActivityConfig setTheme(int resId) {
        if (mActivity != null && mActivity.get() != null) {
            mActivity.get().setTheme(resId);
        } else if (mActivity == null || mActivity.get() == null) {
            Log.e(this, "Activity is null! theme is not set");
        }
        return this;
    }

    public int getDrawerHeaderResId() {
        return mDrawerHeaderResId;
    }

    public int getDrawerMenuIconId() {
        return mDrawerIconId;
    }

    public int getDrawerMenuItemId() {
        return mDrawerMenuId;
    }

    /**
     * set root layout id with DrawerLayout, it will enable toolbar too.
     */
    public ActivityConfig setDrawerResId(@LayoutRes int headerLayoutId, int menuIcons, int menuItems) {
        mDrawerHeaderResId = headerLayoutId;
        mDrawerIconId = menuIcons;
        mDrawerMenuId = menuItems;
        mIsDrawerLayout = true;
        useToolbar();
        return this;
    }

    public boolean isDrawerLayout() {
        return mIsDrawerLayout;
    }

    public int getRootResId() {
        int linearRoot = isDrawerLayout() ? R.layout.activity_drawer : R.layout.activity_toolbar;
        int nestRoot = isDrawerLayout() ? R.layout.activity_drawer_nest : R.layout.activity_toolbar_nest;
        return mIsLinearRoot ? linearRoot : nestRoot;
    }

    @Override
    public String toString() {
        return super.toString() + "\nmToolbarColor=" + mToolbarColor + "\nmDrawerHeaderResId" +
                "=" + mDrawerHeaderResId + "\nmUseToolbar=" + mUseToolbar + "\nmIsLinearRoot=" +
                mIsLinearRoot + "\nmShowHomeAsUpKey=" + mShowHomeAsUpKey + "\nmIsFitSystemWindow=" +
                mIsFitSystemWindow;
    }
}
