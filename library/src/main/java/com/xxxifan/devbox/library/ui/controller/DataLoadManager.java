package com.xxxifan.devbox.library.ui.controller;

import com.xxxifan.devbox.library.tools.Log;

/**
 * Created by xifan on 16-1-14.
 */
public class DataLoadManager {

    private LoadCallbacks mCallback;
    /* indicate whether data is loaded */
    private boolean mIsDataLoaded;
    /* indicate whether data is end */
    private boolean mIsDataEnd;
    private boolean mLazyLoadEnabled;

    private int mPage;

    public static DataLoadManager getDataLoader(LoadCallbacks callbacks) {
        DataLoadManager dataLoadManager = new DataLoadManager();
        dataLoadManager.setCallback(callbacks);
        return dataLoadManager;
    }

    public static DataLoadManager getListDataLoader(ListLoadCallbacks callbacks) {
        DataLoadManager dataLoadManager = new DataLoadManager();
        dataLoadManager.setCallback(callbacks);
        dataLoadManager.notifyPageLoaded(); // init page
        return dataLoadManager;
    }

    public static void destroy(DataLoadManager dataLoadManager) {
        if (dataLoadManager != null) {
            dataLoadManager.setCallback(null);
        }
    }

    private void setCallback(LoadCallbacks callback) {
        if (mCallback != null && callback != null) {
            Log.e(this, "You have set a callback already, did you really want to set it again?");
        }
        mCallback = callback;
    }

    public void onDataLoad() {
        onDataLoad(false);
    }

    public void onLazyDataLoad() {
        onDataLoad(true);
    }

    private void onDataLoad(boolean isLazyLoadMode) {
        if (mCallback == null) {
            return;
        }

        if (!isDataLoaded() && !isDataEnd()) {
            if (isLazyLoadEnabled() && isLazyLoadMode || !isLazyLoadEnabled() && !isLazyLoadMode) {
                boolean isDataLoaded = mCallback.onDataLoad();
                setDataLoaded(isDataLoaded);
            }
        }
    }

    public boolean isDataLoaded() {
        return mIsDataLoaded;
    }

    public void setDataLoaded(boolean loaded) {
        mIsDataLoaded = loaded;
    }

    public boolean isLazyLoadEnabled() {
        return mLazyLoadEnabled;
    }

    public void enableLazyLoad() {
        mLazyLoadEnabled = true;
    }

    public boolean isDataEnd() {
        return mIsDataEnd;
    }

    public void setDataEnd(boolean end) {
        mIsDataEnd = end;
    }

    public void notifyPageLoaded() {
        if (mCallback != null && mCallback instanceof ListLoadCallbacks) {
            mPage += 1;
        }
    }

    public int getPage() {
        return mPage;
    }

    public void disableDataLoad() {
        setDataLoaded(true);
        setDataEnd(true);
    }

    public void resetPage() {
        mPage = 1;
    }

    public interface LoadCallbacks {
        /**
         * load data in this callback, should be called on setUserVisibleHint() at first time and later on onResume()
         *
         * @return true if data load finished, which means it'll not load data again while onResume().
         */
        boolean onDataLoad();
    }

    public interface ListLoadCallbacks extends LoadCallbacks {
        /**
         * setRefresh data list due to loadType, should be called in onDataLoad().
         *
         * @param loadType choose to append result to current list or replace list.
         *                 be one of {@link com.xxxifan.devbox.library.ui.BaseRecyclerFragment#REQUEST_LOAD_MORE}
         *                 or {@link com.xxxifan.devbox.library.ui.BaseRecyclerFragment#REQUEST_REFRESH}
         */
        void onDataRefresh(int loadType);
    }
}
