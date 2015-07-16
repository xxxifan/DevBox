package com.xxxifan.devbox.library.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;
import com.xxxifan.devbox.library.R;
import com.xxxifan.devbox.library.adapter.BasePagerAdapter;

import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by xifan on 15-5-17.
 */
public class BasePagerFragment extends BaseFragment {

    private ViewPager mViewPager;
    private PagerSlidingTabStrip mPagerStrip;
    private BasePagerAdapter mPagerAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_base_pager, container, false);
        mViewPager = ButterKnife.findById(view, R.id.base_viewpager);
        ButterKnife.findById(view, R.id.base_viewpager_strip);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewPager(initFragments(savedInstanceState));
    }

    /**
     * Init fragments, return null if no saved fragments, which means no need to create new instances.
     */
    protected List<Fragment> initFragments(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            List<Fragment> fragmentList = getChildFragmentManager().getFragments();
            if (fragmentList != null && !fragmentList.isEmpty()) {
                return fragmentList;
            }
        }
        return null;
    }

    protected void setupViewPager(List<Fragment> fragments) {
        if (fragments != null && !fragments.isEmpty()) {
            mViewPager.setAdapter(mPagerAdapter = new BasePagerAdapter(getChildFragmentManager(),
                    fragments));
            mPagerStrip.setViewPager(mViewPager);
        }
    }

    protected void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        mPagerStrip.setOnPageChangeListener(listener);
    }

    protected ViewPager getViewPager() {
        return mViewPager;
    }

    protected void setViewPager(ViewPager viewPager) {
        mViewPager = viewPager;
    }

    protected PagerSlidingTabStrip getPagerStrip() {
        return mPagerStrip;
    }

    protected void setPagerStrip(PagerSlidingTabStrip pagerStrip) {
        mPagerStrip = pagerStrip;
    }

    protected BasePagerAdapter getPagerAdapter() {
        return mPagerAdapter;
    }
}
