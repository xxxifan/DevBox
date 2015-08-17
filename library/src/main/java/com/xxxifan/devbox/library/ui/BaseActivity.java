package com.xxxifan.devbox.library.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.xxxifan.devbox.library.R;
import com.xxxifan.devbox.library.adapter.DrawerAdapter;
import com.xxxifan.devbox.library.helpers.ActivityConfig;
import com.xxxifan.devbox.library.helpers.SystemBarTintManager;
import com.xxxifan.devbox.library.tools.Log;

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
    private DrawerLayout mDrawerLayout;

    /**
     * flag to determine proper ActivityConfig setup.
     */
    private boolean mConfigFlag;

    /**
     * get ActivityConfig, for visual configs, call it before super.onCreate()
     */
    protected ActivityConfig getConfig() {
        if (mConfig == null) {
            mConfig = ActivityConfig.newInstance(this);
            if (mConfigFlag) {
                Log.e(this, "ActivityConfig should be called before super.onCreate(), or some config will not be applied");
            }
        }
        return mConfig;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mConfigFlag = true;
    }

    @Override
    public void setContentView(int layoutResID) {
        mConfigFlag = false;
        setContentView(layoutResID, getConfig());
    }

    protected void setContentView(int layoutResID, ActivityConfig config) {
        View rootView;
        if (config.useToolbar()) {
            // set root layout
            rootView = getLayoutInflater().inflate(config.getRootResId(), null, false);
            super.setContentView(rootView);

            View containerView = rootView.findViewById(R.id.toolbar_container);
            if (containerView == null) {
                throw new IllegalStateException("Cannot find toolbar_container");
            }
            containerView.setFitsSystemWindows(config.isFitSystemWindow());

            // attach user layout
            View view = getLayoutInflater().inflate(layoutResID, null, false);
            if (config.isLinearRoot()) {
                ((LinearLayout) containerView).addView(view);
            } else {
                ((FrameLayout) containerView).addView(view, 0);
            }

            // setup toolbar if needed
            Toolbar toolbar = ButterKnife.findById(rootView, R.id.toolbar);
            if (toolbar != null) {
                setupToolbar(toolbar);
                // setup drawer layout if needed, called before initView avoid of NPE
                if (config.isDrawerLayout()) {
                    setupDrawerLayout(rootView);
                }
            }
        } else {
            rootView = getLayoutInflater().inflate(layoutResID, null, false);
            super.setContentView(rootView);
        }

        initView(rootView);
    }

    private void setupDrawerLayout(View rootView) {
        mDrawerLayout = ButterKnife.findById(rootView, R.id.drawer_layout);
        if (mDrawerLayout == null) {
            Log.e(this, "Cannot find DrawerLayout!");
            return;
        }

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, 0, 0);
        mDrawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();

        View headerView = getLayoutInflater().inflate(getConfig().getDrawerHeaderResId(), null);
        ListView drawerListView = ButterKnife.findById(rootView, R.id.drawer_item_list);
        setDrawerAdapter(drawerListView, headerView);
    }

    /**
     * setup drawer item list. You can override it to use a custom adapter.
     */
    protected void setDrawerAdapter(ListView drawerListView, View headerView) {
        final DrawerAdapter drawerAdapter = new DrawerAdapter(drawerListView, getConfig());
        drawerListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        drawerListView.setDivider(new ColorDrawable(getResources().getColor(R.color.transparent)));
        drawerListView.setDividerHeight(0);
        drawerListView.setBackgroundColor(getResources().getColor(R.color.white));
        drawerListView.setCacheColorHint(Color.TRANSPARENT);
        drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            private int lastCheckPosition;

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                if (listView.getHeaderViewsCount() > 0) {
                    position--; // fix wrong pos.
                }
                listView.getCheckedItemPosition();
                if (view.getId() != R.id.drawer_divider && lastCheckPosition != position) {
                    listView.setItemChecked(position, true);
                    lastCheckPosition = position;

                    if (getConfig().getDrawerMenuClickListener() != null) {
                        getConfig().getDrawerMenuClickListener().onMenuClick(view, position);
                    }
                }
            }
        });
        drawerListView.addHeaderView(headerView, null, false);
        drawerListView.setAdapter(drawerAdapter);
        drawerListView.setItemChecked(0, true);
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
            actionBar.setDisplayHomeAsUpEnabled(getConfig().isShowHomeAsUpKey());
        }

        // set compat status color in kitkat or later devices
        if (config.isFitSystemWindow() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
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
            if (getConfig().isDrawerLayout() && mDrawerLayout != null) {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            } else {
                finish();
            }
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
