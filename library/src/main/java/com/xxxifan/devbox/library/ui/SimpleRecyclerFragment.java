package com.xxxifan.devbox.library.ui;

import android.view.View;

/**
 * Empty implement refresh RecyclerFragment
 * Created by xifan on 16-2-1.
 */
public abstract class SimpleRecyclerFragment extends BaseRecyclerFragment {

    @Override
    public void onSetupRefreshView(View view) {

    }

    @Override
    public void onDestroyRefreshView() {

    }

    @Override
    public boolean isRefreshing() {
        return false;
    }

    @Override
    public void setRefresh(boolean refresh) {

    }
}
