package com.xxxifan.devbox.library.ui.controller;

import com.xxxifan.devbox.library.R;

/**
 * Visual configs for BaseRecyclerFragment
 */
public class RecyclerConfig {
    public static final int DEFAULT_FRAGMENT_LAYOUT = R.layout.fragment_recycler;
    public static final int DEFAULT_SWIPE_LAYOUT = R.layout.fragment_recycler_swipe;

    private boolean mEnableScrollListener;
    private int mLayoutResId;

    private RecyclerConfig() {
    }

    public static RecyclerConfig newInstance() {
        RecyclerConfig config = new RecyclerConfig();
        config.setLayoutResId(DEFAULT_FRAGMENT_LAYOUT);
        config.enableScrollListener(true);
        return config;
    }

    public boolean isEnableScrollListener() {
        return mEnableScrollListener;
    }

    public RecyclerConfig enableScrollListener(boolean enableScrollListener) {
        mEnableScrollListener = enableScrollListener;
        return this;
    }

    public int getLayoutResId() {
        return mLayoutResId;
    }

    public RecyclerConfig setLayoutResId(int layoutResId) {
        mLayoutResId = layoutResId;
        return this;
    }
}
