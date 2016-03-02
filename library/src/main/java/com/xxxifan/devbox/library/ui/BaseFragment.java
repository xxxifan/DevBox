package com.xxxifan.devbox.library.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.trello.rxlifecycle.FragmentEvent;
import com.trello.rxlifecycle.FragmentLifecycleProvider;
import com.trello.rxlifecycle.RxLifecycle;
import com.umeng.analytics.MobclickAgent;
import com.xxxifan.devbox.library.Devbox;
import com.xxxifan.devbox.library.callbacks.SimpleSubscriber;
import com.xxxifan.devbox.library.entity.CustomEvent;
import com.xxxifan.devbox.library.tools.Log;
import com.xxxifan.devbox.library.tools.ViewUtils;
import com.xxxifan.devbox.library.ui.controller.ActivityConfig;
import com.xxxifan.devbox.library.ui.controller.ChildUiController;
import com.xxxifan.devbox.library.ui.controller.DataLoadManager;
import com.xxxifan.devbox.library.ui.controller.ToolbarController;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

/**
 * Created by Bob Peng on 2015/5/7.
 */
public abstract class BaseFragment extends Fragment implements FragmentLifecycleProvider {

    private final BehaviorSubject<FragmentEvent> lifecycleSubject = BehaviorSubject.create();

    private MaterialDialog mLoadingDialog;
    private LayoutInflater mInflater;
    private List<ChildUiController> mUiControllers;
    private DataLoadManager mDataLoadManager;
    private Observable<Object> mToolbarSetupObserver;

    private int mLayoutId;
    private String mTabTitle;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        lifecycleSubject.onNext(FragmentEvent.ATTACH);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lifecycleSubject.onNext(FragmentEvent.CREATE);
        setHasOptionsMenu(true);

        Bundle data = getArguments();
        if (data != null) {
            onBundleReceived(data);
        }

        mLayoutId = getLayoutId();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mInflater = inflater;
        View view = inflater.inflate(mLayoutId, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        lifecycleSubject.onNext(FragmentEvent.CREATE_VIEW);
        if (mToolbarSetupObserver != null) {
            mToolbarSetupObserver.subscribe(new SimpleSubscriber<>());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        lifecycleSubject.onNext(FragmentEvent.START);
    }

    @Override
    public void onResume() {
        super.onResume();
        lifecycleSubject.onNext(FragmentEvent.RESUME);
        MobclickAgent.onPageStart(getSimpleName());

        // handle ui controller resume
        if (mUiControllers != null && mUiControllers.size() > 0) {
            for (int i = 0; i < mUiControllers.size(); i++) {
                mUiControllers.get(i).onResume();
            }
        }

        getDataLoadManager().onDataLoad();
    }

    @Override
    public void onPause() {
        lifecycleSubject.onNext(FragmentEvent.PAUSE);
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
    public void onStop() {
        lifecycleSubject.onNext(FragmentEvent.STOP);
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        lifecycleSubject.onNext(FragmentEvent.DESTROY_VIEW);
        super.onDestroyView();
        dismissDialog();

        // unregister ui controllers
        if (mUiControllers != null && mUiControllers.size() > 0) {
            for (int i = 0; i < mUiControllers.size(); i++) {
                mUiControllers.get(i).onDestroy();
            }
            mUiControllers.clear();
            mUiControllers = null;
        }

        DataLoadManager.destroy(mDataLoadManager);
    }

    @Override
    public void onDestroy() {
        lifecycleSubject.onNext(FragmentEvent.DESTROY);
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        lifecycleSubject.onNext(FragmentEvent.DETACH);
        super.onDetach();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        onVisible(isVisibleToUser);

        if (isVisibleToUser) {
            mToolbarSetupObserver = getToolbarObserver();
            if (getActivity() != null) {
                mToolbarSetupObserver.subscribe(new SimpleSubscriber<>());
            }

            getDataLoadManager().onLazyDataLoad();
        }
    }

    /**
     * Called when fragment initialized with a Bundle in onCreate().
     */
    public void onBundleReceived(Bundle data) {
        String title = data.getString(Devbox.EXTRA_TITLE);
        setTabTitle(TextUtils.isEmpty(title) ? "" : title);
    }

    /**
     * for pager fragments, better to load data when user visible, that's time to setLazyDataLoad to
     * true. And this will only works to paper fragments.
     * Will be called before onResume().
     *
     * @param lazyLoad set to false to call onDataLoad() in onResume(), or later in setMenuVisibility().
     */
    protected void setLazyDataLoad(boolean lazyLoad) {
        getDataLoadManager().enableLazyLoad();
    }

    /**
     * notify data loaded and set status to loaded
     */
    public void notifyDataLoaded() {
        getDataLoadManager().setDataLoaded(true);
        getDataLoadManager().notifyPageLoaded();
    }

    public String getTabTitle() {
        return mTabTitle == null ? "" : mTabTitle;
    }

    public void setTabTitle(String title) {
        mTabTitle = title;
    }

    @ColorInt
    protected int getCompatColor(@ColorRes int resId) {
        return ContextCompat.getColor(getContext(), resId);
    }

    protected Drawable getCompatDrawable(@DrawableRes int resId) {
        return ContextCompat.getDrawable(getContext(), resId);
    }

    /**
     * Hacky way to use fragment lifecycle to control dialog
     * You shouldn't use {@link #getLoadingDialog()} anymore
     */
    protected void setCurrentDialog(MaterialDialog newDialog) {
        mLoadingDialog = newDialog;
    }

    public MaterialDialog getLoadingDialog() {
        if (mLoadingDialog == null) {
            mLoadingDialog = ViewUtils.getLoadingDialog(getContext(), null);
        }
        return mLoadingDialog;
    }

    public MaterialDialog getLoadingDialog(String msg) {
        if (mLoadingDialog == null) {
            mLoadingDialog = ViewUtils.getLoadingDialog(getContext(), msg);
        } else {
            mLoadingDialog.setContent(msg);
        }
        return mLoadingDialog;
    }

    public void dismissDialog() {
        ViewUtils.dismissDialog(mLoadingDialog);
    }

    /**
     * @param addToBackStack add current fragment to back stack
     */
    public void checkoutFragment(Fragment fragment, boolean addToBackStack) {
        checkoutFragment(fragment, false, addToBackStack);
    }

    /**
     * @param detach         detach other fragments, if is true, addToBackStack must be false.
     * @param addToBackStack add current fragment to back stack
     */
    public void checkoutFragment(Fragment fragment, boolean detach, boolean addToBackStack) {
        if (fragment == null) {
            return;
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

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        List<Fragment> fragmentList = getFragmentManager().getFragments();
        if (fragmentList != null && fragmentList.size() > 0) {
            // hide other fragment and check if fragment is exist
            for (Fragment oldFragment : fragmentList) {
                if (oldFragment != null && oldFragment.isVisible()) {
                    transaction.hide(oldFragment);
                    oldFragment.setUserVisibleHint(false);
                    if (detach) {
                        transaction.detach(oldFragment);
                    }
                }
            }

            if (addToBackStack) {
                if (detach) {
                    Log.e(this, "You cannot addToBackStack while detach bro");
                } else {
                    transaction.addToBackStack(getTag());
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                }
            }
        }

        if (!fragment.isAdded()) {
            transaction.add(ActivityConfig.DEFAULT_FRAGMENT_CONTAINER_ID, fragment, tag);
        }
        transaction.show(fragment);
        transaction.commitAllowingStateLoss();

        fragment.setUserVisibleHint(true);
    }

    /**
     * setup custom toolbar while fragment shown.
     */
    protected void onSetupToolbar(ToolbarController toolbarController) {
        boolean visible = getBaseActivity().getConfig().isShowHomeAsUpKey();
        toolbarController.setBackButtonVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
    }

    public BaseFragment getBaseFragment() {
        return this;
    }

    protected void postEvent(CustomEvent event, Class target) {
        EventBus.getDefault().post(event);
    }

    protected void postStickyEvent(CustomEvent event, Class target) {
        EventBus.getDefault().postSticky(event);
    }

    protected void registerEventBus(BaseFragment fragment) {
        EventBus.getDefault().registerSticky(fragment);
    }

    protected void unregisterEventBus(BaseFragment fragment) {
        EventBus.getDefault().unregister(fragment);
    }

    /**
     * register controllers, so that BaseFragment can do some lifecycle work automatically
     */
    protected void registerUiController(ChildUiController controller) {
        if (mUiControllers == null) {
            mUiControllers = new ArrayList<>();
        }
        mUiControllers.add(controller);
    }

    protected LayoutInflater getLayoutInflater() {
        return mInflater;
    }

    protected void registerLoadManager(DataLoadManager manager) {
        mDataLoadManager = manager;
    }

    protected DataLoadManager getDataLoadManager() {
        if (mDataLoadManager == null) {
            mDataLoadManager = new DataLoadManager();
        }
        return mDataLoadManager;
    }

    protected <T> Observable.Transformer<T, T> io() {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> observable) {
                return observable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    protected <T> Observable.Transformer<T, T> computation() {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> observable) {
                return observable
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    @Override
    public final Observable<FragmentEvent> lifecycle() {
        return lifecycleSubject.asObservable();
    }

    @Override
    public final <T> Observable.Transformer<T, T> bindUntilEvent(FragmentEvent event) {
        return RxLifecycle.bindUntilFragmentEvent(lifecycleSubject, event);
    }

    @Override
    public final <T> Observable.Transformer<T, T> bindToLifecycle() {
        return RxLifecycle.bindFragment(lifecycleSubject);
    }

    /**
     * a Observable to execute {@link #onSetupToolbar(ToolbarController)},
     * if this observable exists until onResume, then subscribe it.
     */
    private Observable<Object> getToolbarObserver() {
        return Observable
                .create(new Observable.OnSubscribe<Object>() {
                    @Override
                    public void call(Subscriber<? super Object> subscriber) {
                        try {
                            ToolbarController toolbarController = getBaseActivity().getToolbarController();
                            if (toolbarController != null) {
                                onSetupToolbar(toolbarController);
                            }
                            if (!subscriber.isUnsubscribed()) {
                                subscriber.onCompleted();
                            }
                        } catch (Exception e) {
                            if (!subscriber.isUnsubscribed()) {
                                subscriber.onError(e);
                            }
                        }
                    }
                })
                .doOnTerminate(new Action0() {
                    @Override
                    public void call() {
                        mToolbarSetupObserver = null;
                    }
                });
    }

    /**
     * called when {@link #setUserVisibleHint(boolean)}
     * now it will be triggered when checkout fragments, so better to handle events that happens to
     * switch fragments.
     */
    protected void onVisible(boolean visible) {
    }

    @LayoutRes
    protected abstract int getLayoutId();

    protected abstract void initView(View rootView);

    /**
     * @return human readable class name for tracking.
     */
    public abstract String getSimpleName();

}
