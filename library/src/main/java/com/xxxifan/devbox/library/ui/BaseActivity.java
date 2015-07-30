package com.xxxifan.devbox.library.ui;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.xxxifan.devbox.library.R;
import com.xxxifan.devbox.library.helpers.ActivityConfig;
import com.xxxifan.devbox.library.helpers.SystemBarTintManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by Administrator on 2015/5/6.
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected Context mContext;

    private ActivityConfig mConfig;
    private SystemBarTintManager mSystemBarManager;
    private List<UiController> mUiControllers;

    /**
     * get ActivityConfig, for visual configs, call it before super.onCreate()
     *
     * @return
     */
    protected ActivityConfig getConfig() {
        if (mConfig == null) {
            mConfig = ActivityConfig.newInstance(this);
        }
        return mConfig;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
    }

    @Override
    public void setContentView(int layoutResID) {
        setContentView(layoutResID, getConfig());
    }

    protected void setContentView(int layoutResID, ActivityConfig config) {
        View activityView;
        if (config.useToolbar()) {
            activityView = getLayoutInflater().inflate(config.isLinearRoot() ?
                    R.layout.activity_toolbar : R.layout.activity_toolbar_nest, null, false);
            super.setContentView(activityView);
            View view = getLayoutInflater().inflate(layoutResID, null, false);
            activityView.setFitsSystemWindows(!config.isTransparentBar());

            if (config.isLinearRoot()) {
                ((LinearLayout) activityView).addView(view);
            } else {
                ((FrameLayout) activityView).addView(view, 0);
            }

            Toolbar toolbar = ButterKnife.findById(activityView, R.id.toolbar);
            if (toolbar != null) {
                setupToolbar(toolbar);
            }
        } else {
            activityView = getLayoutInflater().inflate(layoutResID, null, false);
            super.setContentView(activityView);
        }

        initView(activityView);
    }

    /**
     * setup toolbar
     */
    protected void setupToolbar(@NonNull Toolbar toolbar) {
        ActivityConfig config = getConfig();
        toolbar.setBackgroundColor(config.getToolbarColor());
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(getConfig().showHomeAsUpKey());
        }

        // set compat status color in kitkat or later devices
        if (!config.isTransparentBar() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (mSystemBarManager == null) {
                mSystemBarManager = new SystemBarTintManager(this);
            }
            mSystemBarManager.setStatusBarTintEnabled(true);
            mSystemBarManager.setTintColor(config.getToolbarColor());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // unregister ui controllers
        if (mUiControllers != null && !mUiControllers.isEmpty()) {
            for (int i = 0; i < mUiControllers.size(); i++) {
                mUiControllers.get(i).onDestroy();
            }
            mUiControllers.clear();
            mUiControllers = null;
        }
    }

    /**
     * register controllers, so that BaseActivity can do some work automatically
     *
     * @param controller
     */
    protected void registerUiController(UiController controller) {
        if (mUiControllers == null) {
            mUiControllers = new ArrayList<>();
        }
        mUiControllers.add(controller);
    }

    protected void hideToolBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    protected void showToolBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
    }

    protected Context getContext() {
        return mContext;
    }

    /**
     * @param rootView the root of user layout
     */
    protected abstract void initView(View rootView);
}
