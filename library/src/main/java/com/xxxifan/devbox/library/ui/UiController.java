package com.xxxifan.devbox.library.ui;

import android.content.Context;
import android.view.View;

/**
 * Created by xifan on 15-7-22.
 */
public abstract class UiController {
    private View mView;

    public UiController(View view) {
        if (view == null) {
            throw new IllegalArgumentException("view cannot be null");
        }
        mView = view;
    }

    public View getRootView() {
        return mView;
    }

    public void onResume() {
    }

    public void onPause() {
    }

    protected Context getContext() {
        return mView == null ? null : mView.getContext();
    }

    public void onDestroy() {
        mView = null;
    }
}
