package com.xxxifan.devbox.library.helpers;

import android.view.View;

import com.xxxifan.devbox.library.ui.BaseRecyclerFragment;
import com.xxxifan.devbox.library.ui.controller.ChildUiController;

/**
 * Created by xifan on 16-2-1.
 */
public class PullRefreshHandler extends ChildUiController {

    private View mPullView;
    private PullLayoutCallback mCallback;

    public PullRefreshHandler(BaseRecyclerFragment fragment, View view) {
        super(fragment, view);
        mPullView = view;
    }

    @Override
    protected void initView(View view) {
    }

    public void setPullLayout(PullLayoutCallback callback) {
        mCallback = callback;
        callback.onSetupRefreshView(getView());
    }

    public void postRefresh(final boolean refresh) {
        mPullView.post(new Runnable() {
            @Override
            public void run() {
                mCallback.setRefresh(refresh);
            }
        });
    }

    public void setRefresh(boolean refresh) {
        mCallback.setRefresh(refresh);
    }

    @Override
    public void onDestroy() {
        mCallback.onDestroyRefreshView();
        mCallback = null;
        mPullView = null;
        super.onDestroy();
    }

    public boolean isRefreshing() {
        return mCallback.isRefreshing();
    }

    public interface PullLayoutCallback {
        void onSetupRefreshView(View view);

        void onDestroyRefreshView();

        boolean isRefreshing();

        void setRefresh(boolean refresh);
    }
}
