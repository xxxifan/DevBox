package com.xxxifan.devbox.library.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;

import com.trello.rxlifecycle.ActivityEvent;
import com.trello.rxlifecycle.ActivityLifecycleProvider;
import com.trello.rxlifecycle.RxLifecycle;
import com.umeng.analytics.MobclickAgent;
import com.xxxifan.devbox.library.Devbox;
import com.xxxifan.devbox.library.R;
import com.xxxifan.devbox.library.entity.CustomEvent;
import com.xxxifan.devbox.library.helpers.SystemBarTintManager;
import com.xxxifan.devbox.library.tools.IOUtils;
import com.xxxifan.devbox.library.tools.Log;
import com.xxxifan.devbox.library.tools.Utils;
import com.xxxifan.devbox.library.ui.controller.ActivityConfig;
import com.xxxifan.devbox.library.ui.controller.ToolbarController;
import com.xxxifan.devbox.library.ui.controller.UiController;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.subjects.BehaviorSubject;

/**
 * Created by Administrator on 2015/5/6.
 */
public abstract class BaseActivity extends AppCompatActivity implements ActivityLifecycleProvider {

    private static final int BASE_CONTAINER_ID = R.id.base_container;
    private static final int TOOLBAR_STUB_ID = R.id.toolbar_stub;
    private static final int TOOLBAR_DARK_LAYOUT = R.layout.view_toolbar_dark;
    private static final int TOOLBAR_LIGHT_LAYOUT = R.layout.view_toolbar_light;
    private static final int TOOLBAR_CUSTOM_LAYOUT = R.layout.view_custom_toolbar;
    private static final int TOOLBAR_CUSTOM_ID = R.id.toolbar_custom;
    private static final int TOOLBAR_DEFAULT_ID = R.id.toolbar;

    private final BehaviorSubject<ActivityEvent> lifecycleSubject = BehaviorSubject.create();

    private ActivityConfig mConfig;
    private SystemBarTintManager mSystemBarManager;
    private List<UiController> mUiControllers;
    private ToolbarController mToolbarController;
    private ToolbarController.Handler mToolbarHandler;
    private BaseFragment mCurrentFragment;

    private BackKeyListener mBackKeyListener;

    /**
     * get ActivityConfig, for visual configs, call it before super.onCreate()
     */
    protected ActivityConfig getConfig() {
        if (mConfig == null) {
            mConfig = ActivityConfig.newInstance(this);
        }
        return mConfig;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        onConfigureActivity(getConfig());
        super.onCreate(savedInstanceState);
        lifecycleSubject.onNext(ActivityEvent.CREATE);
        setContentView(getLayoutId());

        // handle fragments
        List<Fragment> fragmentList = savedInstanceState == null ? null :
                getSupportFragmentManager().getFragments();
        if (fragmentList != null && fragmentList.size() > 0) {
            onCreateFragment(fragmentList);
        } else {
            onCreateFragment(null);
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        setContentView(layoutResID, getConfig());
    }

    @Override
    protected void onStart() {
        super.onStart();
        lifecycleSubject.onNext(ActivityEvent.START);
    }

    @Override
    protected void onResume() {
        super.onResume();
        lifecycleSubject.onNext(ActivityEvent.RESUME);
        MobclickAgent.onPageStart(getSimpleName());

        // handle ui controller resume
        if (mUiControllers != null && mUiControllers.size() > 0) {
            for (int i = 0; i < mUiControllers.size(); i++) {
                mUiControllers.get(i).onResume();
            }
        }
    }

    @Override
    protected void onPause() {
        lifecycleSubject.onNext(ActivityEvent.PAUSE);
        super.onPause();
        MobclickAgent.onPageEnd(getSimpleName());

        // handle ui controller pause
        if (mUiControllers != null && mUiControllers.size() > 0) {
            for (int i = 0; i < mUiControllers.size(); i++) {
                mUiControllers.get(i).onPause();
            }
        }
    }

    @Override
    protected void onStop() {
        lifecycleSubject.onNext(ActivityEvent.STOP);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        lifecycleSubject.onNext(ActivityEvent.DESTROY);
        super.onDestroy();
        // unregister ui controllers
        if (mUiControllers != null && mUiControllers.size() > 0) {
            for (int i = 0; i < mUiControllers.size(); i++) {
                mUiControllers.get(i).onDestroy();
            }
            mUiControllers.clear();
            mUiControllers = null;
        }
        mToolbarController = null;
        mToolbarHandler = null;
    }

    @Override
    public void onBackPressed() {
        if (mBackKeyListener == null || !mBackKeyListener.onBackKeyPressed()) {
            super.onBackPressed();
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

    protected void setContentView(int layoutResID, ActivityConfig config) {
        if (config.useToolbar()) {
            // set root layout
            super.setContentView(config.getRootResId());

            View containerView = findViewById(BASE_CONTAINER_ID);
            if (containerView == null) {
                throw new IllegalStateException("Cannot find toolbar_container");
            }
            containerView.setFitsSystemWindows(config.isFitSystemWindow());

            // attach user layout
            if (layoutResID > 0) {
                View view = getLayoutInflater().inflate(layoutResID, null, false);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-1, -1);
                if (!config.isToolbarTransparent()) {
                    params.topMargin = getResources().getDimensionPixelSize(R.dimen.toolbar_height);
                }
                ((FrameLayout) containerView).addView(view, 0, params);
            }

            // setup toolbar if needed
            ViewStub toolbarStub = ButterKnife.findById(this, TOOLBAR_STUB_ID);
            if (toolbarStub != null) {
                if (!config.isCustomToolbar()) {
                    toolbarStub.setLayoutResource(config.isDarkToolbar() ? TOOLBAR_DARK_LAYOUT
                            : TOOLBAR_LIGHT_LAYOUT);
                } else {
                    toolbarStub.setLayoutResource(config.getCustomToolbarId() == 0 ?
                            TOOLBAR_CUSTOM_LAYOUT : config.getCustomToolbarId());
                }

                View toolbarStubView = toolbarStub.inflate();
                if (config.isToolbarTransparent()) {
                    ((ViewGroup.MarginLayoutParams) toolbarStubView.getLayoutParams()).topMargin = Utils.getSystemBarHeight();
                }
                setupToolbar(toolbarStubView);
            } else {
                View toolbarView = ButterKnife.findById(this, config.isCustomToolbar() ?
                        TOOLBAR_CUSTOM_ID : TOOLBAR_DEFAULT_ID);
                if (toolbarView != null) {
                    if (config.isToolbarTransparent()) {
                        ((ViewGroup.MarginLayoutParams) toolbarView.getLayoutParams()).topMargin = Utils.getSystemBarHeight();
                    }
                    setupToolbar(toolbarView);
                } else {
                    Log.e(this, "No available toolbar exists");
                }
            }

            // set compat status color in kitkat or later devices
            if (config.isFitSystemWindow() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (mSystemBarManager == null) {
                    mSystemBarManager = new SystemBarTintManager(this);
                }
                mSystemBarManager.setStatusBarTintEnabled(true);
                mSystemBarManager.setTintColor(config.getToolbarColor());
            }
        } else {
            if (layoutResID > 0) {
                super.setContentView(layoutResID);
            }
        }

        // custom title
        String title = getIntent().getStringExtra(Devbox.EXTRA_TITLE);
        if (!TextUtils.isEmpty(title)) {
            setTitle(title);
        }

        initView(getWindow().getDecorView());
    }

    /**
     * setup toolbar
     * build-in toolbar will not use a ToolbarController
     */
    protected ToolbarController setupToolbar(@NonNull View toolbarView) {
        Toolbar toolbar = (Toolbar) toolbarView;
        toolbar.setBackgroundColor(getConfig().getToolbarColor());
        setSupportActionBar(toolbar);
        // set toolbar function
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(getConfig().isShowHomeAsUpKey());
        }
        return null;
    }

    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(mToolbarController != null ? "" : title, color);
        if (mToolbarController != null) {
            mToolbarController.setTitleText(title);
        }
    }

    public void setToolbarLeftButton(Drawable icon, View.OnClickListener listener) {
        if (mToolbarController != null) {
            mToolbarController.setBackButton(icon, listener);
        }
    }

    public void setToolbarRightButton(Drawable icon, View.OnClickListener listener) {
        if (mToolbarController != null) {
            mToolbarController.setRightButton(icon, listener);
        }
    }

    public void setTitleTextSize(int size) {
        if (mToolbarController != null) {
            mToolbarController.setTitleTextSize(size);
        }
    }

    public void setTitleTextColor(@ColorInt int color) {
        if (mToolbarController != null) {
            mToolbarController.setTitleColor(color);
        }
    }

    /**
     * proper time to create fragment
     *
     * @param savedFragments fragments from savedInstanceBundle
     */
    protected void onCreateFragment(List<Fragment> savedFragments) {

    }

    /**
     * set fragment will be attach to this activity right now
     */
    public void setContainerFragment(Fragment fragment) {
        setContainerFragment(fragment, getConfig().getContainerId(), false);
    }

    /**
     * set fragment will be attach to this activity right now
     *
     * @param detach if true will detach other fragment instead of hide.
     */
    public void setContainerFragment(Fragment fragment, boolean detach) {
        setContainerFragment(fragment, getConfig().getContainerId(), detach);
    }

    /**
     * set fragment will be attach to this activity right now
     *
     * @param containerId the target containerId will be attached to.
     */
    public void setContainerFragment(Fragment fragment, @IdRes int containerId) {
        setContainerFragment(fragment, containerId, false);
    }

    /**
     * set fragment will be attach to this activity right now
     *
     * @param containerId the target containerId will be attached to.
     * @param detach      if true will detach other fragment instead of hide.
     */
    public void setContainerFragment(Fragment fragment, @IdRes int containerId, boolean detach) {
        if (fragment == null || fragment == mCurrentFragment) {
            return;
        }
        if (fragment instanceof BaseFragment) {
            mCurrentFragment = (BaseFragment) fragment;
        }

        // get tag name
        String tag = fragment.getTag();
        if (TextUtils.isEmpty(tag)) {
            if (fragment instanceof BaseFragment) {
                tag = ((BaseFragment) fragment).getSimpleName();
            } else {
                tag = fragment.getClass().getName();
            }
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        if (fragmentList != null && fragmentList.size() > 0) {
            // hide other fragment and check if fragment is exist
            for (Fragment oldFragment : fragmentList) {
                if (oldFragment == null) {
                    continue;
                }

                if (tag.equals(oldFragment.getTag()) && fragment != oldFragment) {
                    transaction.hide(oldFragment);
                    transaction.remove(oldFragment);
                    transaction.detach(oldFragment);
                } else if (oldFragment.isVisible()) {
                    transaction.hide(oldFragment);
                    oldFragment.setUserVisibleHint(false);
                    if (detach) {
                        transaction.detach(oldFragment);
                    }
                }
            }
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        }

        if (!fragment.isAdded()) {
            transaction.add(containerId, fragment, tag);
        }
        transaction.show(fragment);
        transaction.commitAllowingStateLoss();

        // manually call setUserVisibleHint to notify it'll be visible soon.
        fragment.setUserVisibleHint(true);
    }

    @ColorInt
    public int getCompatColor(@ColorRes int resId) {
        return ContextCompat.getColor(this, resId);
    }

    public Drawable getCompatDrawable(@DrawableRes int resId) {
        return ContextCompat.getDrawable(this, resId);
    }

    protected Fragment getFragment(String tag) {
        return getSupportFragmentManager().findFragmentByTag(tag);
    }

    /**
     * register controllers, so that BaseActivity can do some lifecycle work automatically
     */
    protected void registerUiControllers(UiController... controllers) {
        if (controllers == null) {
            Log.e(this, "controllers cannot be empty");
            return;
        }
        if (mUiControllers == null) {
            mUiControllers = new ArrayList<>();
        }

        for (int i = 0, s = controllers.length; i < s; i++) {
            mUiControllers.add(controllers[i]);
        }
    }

    protected void unregisterUiController(UiController uiController) {
        if (mUiControllers != null && mUiControllers.size() > 0) {
            mUiControllers.remove(uiController);
        }
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

    protected void registerEventBus(Object object) {
        EventBus eventBus = EventBus.getDefault();
        if (!eventBus.isRegistered(object)) {
            eventBus.register(object);
        }
    }

    protected void unregisterEventBus(Object object) {
        EventBus eventBus = EventBus.getDefault();
        if (eventBus.isRegistered(object)) {
            eventBus.unregister(object);
        }
    }

    protected void postEvent(CustomEvent event, Class target) {
        EventBus.getDefault().post(event);
    }

    protected void postStickyEvent(CustomEvent event, Class target) {
        EventBus.getDefault().postSticky(event);
    }

    protected Context getContext() {
        return this;
    }

    public BaseFragment getCurrentFragment() {
        return mCurrentFragment;
    }

    public ToolbarController getToolbarController() {
        return mToolbarController;
    }

    public void setToolbarController(ToolbarController toolbarController) {
        if (mToolbarController != null) {
            unregisterUiController(mToolbarController);
        }
        mToolbarController = toolbarController;
        registerUiControllers(mToolbarController);
    }

    public ToolbarController.Handler getCustomToolbarHandler() {
        return mToolbarHandler;
    }

    public void setCustomToolbarHandler(ToolbarController.Handler handler) {
        mToolbarHandler = handler;
    }

    public void setBackKeyListener(BackKeyListener listener) {
        mBackKeyListener = listener;
    }

    public Observable<ActivityEvent> lifecycle() {
        return lifecycleSubject.asObservable();
    }

    @Override
    public <T> Observable.Transformer<T, T> bindUntilEvent(ActivityEvent event) {
        return RxLifecycle.bindUntilActivityEvent(lifecycleSubject, event);
    }

    @Override
    public <T> Observable.Transformer<T, T> bindToLifecycle() {
        return RxLifecycle.bindActivity(lifecycleSubject);
    }

    protected <T> Observable.Transformer<T, T> io() {
        return IOUtils.io();
    }

    protected <T> Observable.Transformer<T, T> computation() {
        return IOUtils.computation();
    }

    /**
     * Set ActivityConfig, called before super.onCreate()
     */
    protected abstract void onConfigureActivity(ActivityConfig config);

    /**
     * @return activity layout id, called while setContentView()
     */
    @LayoutRes
    protected abstract int getLayoutId();

    /**
     * @param rootView the root of user layout
     */
    protected abstract void initView(View rootView);

    /**
     * @return human readable class name for tracking.
     */
    public abstract String getSimpleName();

    public interface BackKeyListener {
        boolean onBackKeyPressed();
    }
}
