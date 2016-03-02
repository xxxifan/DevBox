package com.xxxifan.devbox.library.ui;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

import com.xxxifan.devbox.library.R;
import com.xxxifan.devbox.library.helpers.PullRefreshHandler;
import com.xxxifan.devbox.library.ui.controller.DataLoadManager;
import com.xxxifan.devbox.library.ui.controller.RecyclerConfig;

import butterknife.ButterKnife;


/**
 * Created by Bob Peng on 2015/5/12.
 */
public abstract class BaseRecyclerFragment extends BaseFragment implements DataLoadManager.ListLoadCallbacks, PullRefreshHandler.PullLayoutCallback {

    public static final int VIEW_TYPE_HEADER = 1;
    public static final int VIEW_TYPE_ITEM = 0;
    public static final int VIEW_TYPE_SPACER = 2;
    public static final int VIEW_TYPE_OTHER = -1;

    protected static final int REQUEST_PAGE_SIZE = 20;
    protected static final int REQUEST_NO_ID = -1;
    protected static final int REQUEST_LOAD_MORE = 1;
    protected static final int REQUEST_REFRESH = 0;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private ViewStub mEmptyStub;
    private View mEmptyView;
    private PullRefreshHandler mRefreshHandler;

    private RecyclerConfig mConfig;

    protected RecyclerConfig getConfig() {
        if (mConfig == null) {
            mConfig = RecyclerConfig.newInstance();
        }
        return mConfig;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        onConfigureFragment(getConfig());
        super.onCreate(savedInstanceState);
    }

    protected void onConfigureFragment(RecyclerConfig config) {

    }

    @Override
    protected int getLayoutId() {
        return getConfig().getLayoutResId();
    }

    @Override
    protected void initView(View rootView) {
        registerLoadManager(DataLoadManager.getListDataLoader(this));
        // setup recycler view
        View childView = ButterKnife.findById(rootView, R.id.fragment_recycler_view);
        if (childView != null) {
            mRecyclerView = (RecyclerView) childView;
            setupRecyclerView(mRecyclerView);
        } else {
            throw new IllegalStateException("RecyclerView not found");
        }

        // setup refresh layout
        View layoutView = rootView.findViewById(R.id.fragment_recycler_swipe_layout);
        if (layoutView != null) {
            mRefreshHandler = new PullRefreshHandler(this, layoutView);
            mRefreshHandler.setPullLayout(this);

            registerUiController(mRefreshHandler);
        }

        mEmptyStub = ButterKnife.findById(rootView, R.id.recycler_empty_view);
    }

    protected void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        if (getConfig().isEnableScrollListener()) {
            recyclerView.addOnScrollListener(new ScrollListener());
        }

        if (mAdapter == null) {
            mAdapter = new BaseRecyclerAdapter();
        }
        recyclerView.setAdapter(mAdapter);
    }

    /**
     * No need to override or call this method when you extended from BaseRecyclerFragment
     * you can simply override its return value to control isDataLoaded() status
     */
    @Override
    public boolean onDataLoad() {
        startRefresh();
        return false;
    }

    /**
     * start refresh animation and run as load more type.
     */
    protected void startRefresh() {
        startRefresh(REQUEST_REFRESH);
    }

    protected void startRefresh(int loadType) {
        if (loadType == REQUEST_REFRESH) {
            getDataLoadManager().resetPage();
            if (mRefreshHandler != null) {
                mRefreshHandler.postRefresh(true);
            }
        }
        onDataRefresh(loadType);
    }

    protected void stopRefresh() {
        if (mRefreshHandler != null) {
            mRefreshHandler.setRefresh(false);
        }
    }

    @Override
    public void notifyDataLoaded() {
        super.notifyDataLoaded();
        try {
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        } catch (IllegalStateException ignore) {
        }

        if (mRefreshHandler != null && mRefreshHandler.isRefreshing()) {
            stopRefresh();
        }
    }

    public void setEmptyView(int layoutRes) {
        if (mEmptyStub != null) {
            mEmptyStub.setLayoutResource(layoutRes);
        } else if (getRecyclerView() != null) {
            mEmptyView = View.inflate(getContext(), layoutRes, (ViewGroup) getRecyclerView().getParent());
            mEmptyView.setVisibility(View.GONE);
        }
    }

    public void showEmptyView() {
        if (mEmptyView == null) {
            if (mEmptyStub == null) {
                return;
            }
            mEmptyView = mEmptyStub.inflate();
        }

        mEmptyView.setVisibility(View.VISIBLE);
    }

    public void hideEmptyView() {
        if (mEmptyView != null) {
            mEmptyView.setVisibility(View.GONE);
        }
    }

    protected boolean isDataEnd() {
        return getDataLoadManager().isDataEnd();
    }

    /**
     * should call this method if data is loaded successfully but none data returned, which means
     * no more data present.
     */
    protected void setDataEnd(boolean isEnd) {
        getDataLoadManager().setDataEnd(isEnd);
    }

    protected boolean isDataLoaded() {
        return getDataLoadManager().isDataLoaded();
    }

    protected void onScrolledToEnd() {
        startRefresh(REQUEST_LOAD_MORE);
    }

    protected int getPage() {
        return getDataLoadManager().getPage();
    }

    protected int getExtraItemSize() {
        return 0;
    }

    protected RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    protected RecyclerView.Adapter getAdapter() {
        return mAdapter;
    }

    protected void setAdapter(RecyclerView.Adapter adapter) {
        mAdapter = adapter;
    }

    protected PullRefreshHandler getRefreshHandler() {
        return mRefreshHandler;
    }

    /**
     * Delegate method to generate view holder.
     */
    protected abstract RecyclerView.ViewHolder createViewHolder(ViewGroup parent, int viewType);

    protected abstract void bindViewHolder(RecyclerView.ViewHolder holder, int position);

    protected abstract int getViewType(int position);

    protected abstract int getCount();

    private class BaseRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return BaseRecyclerFragment.this.createViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            BaseRecyclerFragment.this.bindViewHolder(holder, position);
        }

        @Override
        public int getItemCount() {
            return BaseRecyclerFragment.this.getCount();
        }

        @Override
        public int getItemViewType(int position) {
            return BaseRecyclerFragment.this.getViewType(position);
        }
    }

    private class ScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (dy > 0 && !recyclerView.canScrollVertically(1) && !isDataEnd()) {
                onScrolledToEnd();
            }
        }
    }
}
