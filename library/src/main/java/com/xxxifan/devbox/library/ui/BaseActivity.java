package com.xxxifan.devbox.library.ui;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xxxifan.devbox.library.AppConfig;
import com.xxxifan.devbox.library.R;
import com.xxxifan.devbox.library.SystemBarTintManager;

import butterknife.ButterKnife;

/**
 * Created by Administrator on 2015/5/6.
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected Context mContext;

    private SystemBarTintManager mSystemBarManager;
    private TextView mTitleView;
    private int mThemeColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setupSystemBar();
        super.onCreate(savedInstanceState);

        mContext = this;
    }

    /**
     * set system bar translucent effect
     */
    protected void setupSystemBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        setContentView(layoutResID, true);
    }

    protected void setContentView(int layoutResID, boolean useToolbar) {
        setContentView(layoutResID, useToolbar, true, useToolbar ? getResources().getColor(R.color
                .colorPrimary) : 0);
    }

    /**
     * @param layoutResID layout id
     * @param colorResId  toolbar color resource id
     */
    protected void setContentView(int layoutResID, @ColorRes int colorResId) {
        setContentView(layoutResID, true, true, getResources().getColor(colorResId));
    }

    /**
     * @param isLinear use LinearLayout or FrameLayout for toolbar and content
     * @param color    toolbar color
     */
    protected void setContentView(int layoutResID, boolean useToolbar, boolean isLinear, @ColorInt int
            color) {
        if (useToolbar) {
            super.setContentView(isLinear ? R.layout.activity_toolbar : R.layout.activity_toolbar_nest);
            View view = getLayoutInflater().inflate(layoutResID, null, false);
            if (isLinear) {
                ((LinearLayout) findViewById(R.id.toolbar_container)).addView(view);
            } else {
                ((FrameLayout) findViewById(R.id.toolbar_container)).addView(view, 0);
            }
            setupToolbar(color);
        } else {
            super.setContentView(layoutResID);
        }

        initView();
    }

    /**
     * setup toolbar
     */
    protected Toolbar setupToolbar(@ColorInt int themeColor) {
        Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(themeColor);
            setSupportActionBar(toolbar);
            ActionBar bar = getSupportActionBar();
            if (bar != null && mTitleView == null) {
                View view = findViewById(R.id.toolbar_title);
                if (view != null) {
                    mTitleView = (TextView) view;
                }

                bar.setDisplayHomeAsUpEnabled(true);
            }
        }
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // set compat status color in kitkat or later devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (mSystemBarManager == null) {
                mSystemBarManager = new SystemBarTintManager(this);
            }
            mSystemBarManager.setStatusBarTintEnabled(AppConfig.TRANSLUCENT_BAR_ENABLED);
            mSystemBarManager.setTintColor(themeColor);
        }

        mThemeColor = themeColor;
        return toolbar;
    }

    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
        if (mTitleView != null) {
            mTitleView.setText(title);
        }
    }

    protected void hideToolBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
            mTitleView.setVisibility(View.GONE);
        }
    }

    protected void showToolBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().show();
            mTitleView.setVisibility(View.VISIBLE);
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

    protected TextView getTitleView() {
        return mTitleView;
    }

    protected int getThemeColor() {
        return mThemeColor;
    }

    protected abstract void initView();
}
