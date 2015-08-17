package com.xxxifan.devbox.library.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.afollestad.materialdialogs.MaterialDialog;
import com.xxxifan.devbox.library.Keys;
import com.xxxifan.devbox.library.tools.ViewUtils;

/**
 * Created by Bob Peng on 2015/5/7.
 */
public class BaseFragment extends Fragment {
    protected Context mContext;

    private MaterialDialog mLoadingDialog;

    private boolean mIsDataLoaded = false;
    private boolean mLazyLoad = false;
    private String mTabTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        setHasOptionsMenu(true);

        Bundle data = getArguments();
        if (data != null) {
            onBundleReceived(data);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mIsDataLoaded && !mLazyLoad) {
            setDataLoaded(onDataLoad());
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && !mIsDataLoaded && mLazyLoad) {
            setDataLoaded(onDataLoad());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dismissDialog();
    }

    protected Context getContext() {
        return mContext;
    }

    /**
     * Called when fragment initialized with a Bundle in onCreate().
     */
    public void onBundleReceived(Bundle data) {
        String title = data.getString(Keys.EXTRA_TITLE);
        setTabTitle(TextUtils.isEmpty(title) ? "" : title);
    }

    /**
     * for pager fragments, better to load data when user visible, that's time to setLazyDataLoad to
     * true.
     * called before onResume().
     *
     * @param lazyLoad set to false to call onDataLoad() in onResume(), or later in setMenuVisibility().
     */
    protected void setLazyDataLoad(boolean lazyLoad) {
        mLazyLoad = lazyLoad;
    }

    /**
     * a good point to load data, called on setMenuVisibility() at first time and later on onResume().
     *
     * @return whether data load successful.
     */
    protected boolean onDataLoad() {
        return false;
    }

    /**
     * notify data loaded and set status to loaded
     */
    protected void notifyDataLoaded() {
        setDataLoaded(true);
    }

    protected boolean isDataLoaded() {
        return mIsDataLoaded;
    }

    protected void setDataLoaded(boolean value) {
        mIsDataLoaded = value;
    }

    public String getTabTitle() {
        return mTabTitle == null ? "" : mTabTitle;
    }

    public void setTabTitle(String title) {
        mTabTitle = title;
    }

    protected MaterialDialog getLoadingDialog() {
        if (mLoadingDialog == null) {
            mLoadingDialog = ViewUtils.getLoadingDialog(getContext(), null);
        }
        return mLoadingDialog;
    }

    protected void setLoadingDialog(MaterialDialog dialog) {
        mLoadingDialog = dialog;
    }

    protected MaterialDialog getLoadingDialog(String msg) {
        if (mLoadingDialog == null) {
            mLoadingDialog = ViewUtils.getLoadingDialog(getContext(), msg);
        } else {
            mLoadingDialog.setContent(msg);
        }
        return mLoadingDialog;
    }

    protected void dismissDialog() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

}
