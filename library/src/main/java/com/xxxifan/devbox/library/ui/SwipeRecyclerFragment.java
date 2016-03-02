package com.xxxifan.devbox.library.ui;


import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;

import com.xxxifan.devbox.library.R;
import com.xxxifan.devbox.library.ui.controller.RecyclerConfig;

/**
 * Created by xifan on 16-2-1.
 */
public abstract class SwipeRecyclerFragment extends BaseRecyclerFragment {

    @Override
    protected void onConfigureFragment(RecyclerConfig config) {
        super.onConfigureFragment(config);
        config.setLayoutResId(RecyclerConfig.DEFAULT_SWIPE_LAYOUT);
    }

    @Override
    public void onSetupRefreshView(View view) {
        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) view;
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getDataLoadManager().resetPage();
                onDataRefresh(REQUEST_REFRESH);
            }
        });
    }

    @Override
    public void setRefresh(boolean refresh) {
        getSwipeRefreshLayout().setRefreshing(refresh);
    }

    @Override
    public boolean isRefreshing() {
        return getSwipeRefreshLayout().isRefreshing();
    }

    @Override
    public void onDestroyRefreshView() {
        if (getSwipeRefreshLayout() != null) {
            getSwipeRefreshLayout().setOnRefreshListener(null);
        }
    }

    protected SwipeRefreshLayout getSwipeRefreshLayout() {
        if (getRefreshHandler() == null) {
            return null;
        }
        return ((SwipeRefreshLayout) getRefreshHandler().getView());
    }
}
