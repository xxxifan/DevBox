package com.xxxifan.devbox.library.helpers;

import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.view.View;
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

    private DrawerMenuClickListener mMenuClickListener;

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
        config.setFitSystemWindow(true);
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

    /**
     *  configure to use toolbar, default true
     */
    public ActivityConfig setUseToolbar(boolean useToolbar) {
        mUseToolbar = useToolbar;
        return this;
    }

    public boolean isShowHomeAsUpKey() {
        return mShowHomeAsUpKey;
    }

    /**
     * configure to show home as up key, default true
     */
    public ActivityConfig setShowHomeAsUpKey(boolean enable) {
        mShowHomeAsUpKey = enable;
        return this;
    }

    public boolean isLinearRoot() {
        return mIsLinearRoot;
    }

    /**
     * configure to whether is linear root layout, default true
     */
    public ActivityConfig setIsLinearRoot(boolean isLinearRoot) {
        mIsLinearRoot = isLinearRoot;
        return this;
    }

    /**
     * whether enable translucent status bar, default true
     */
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

    /**
     * whether enable translucent nav bar, default false
     */
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

    /**
     * @return set container layout to use fit system window, default false
     */
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
     * set root layout id with DrawerLayout and enable drawerLayout, it will enable toolbar too.
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

    public ActivityConfig setDrawerMenuClickListener(DrawerMenuClickListener listener) {
        mMenuClickListener = listener;
        return this;
    }

    public DrawerMenuClickListener getDrawerMenuClickListener() {
        return mMenuClickListener;
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

    public interface DrawerMenuClickListener{
        void onMenuClick(View v, int position);
    }
}
