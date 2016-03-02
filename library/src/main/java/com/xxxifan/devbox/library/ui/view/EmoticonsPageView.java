package com.xxxifan.devbox.library.ui.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.xxxifan.devbox.library.adapter.EmoticonsAdapter;
import com.xxxifan.devbox.library.entity.EmoticonBean;
import com.xxxifan.devbox.library.entity.EmoticonSetBean;
import com.xxxifan.devbox.library.tools.Utils;
import com.xxxifan.devbox.library.tools.ViewUtils;

import java.util.ArrayList;
import java.util.List;

import static com.xxxifan.devbox.library.adapter.EmoticonsAdapter.EmoticonsListener;

public class EmoticonsPageView extends ViewPager implements EmoticonsListener {

    public int mOldPagePosition = -1;
    private Context mContext;
    private int mHeight = 0;
    private int mMaxEmoticonSetPageCount = 0;
    private int mCurrentViewCount = -1;
    private int mCurrentPage;
    private List<EmoticonSetBean> mEmoticonSetBeanList;
    private EmoticonsViewPagerAdapter mEmoticonsViewPagerAdapter;
    private ArrayList<View> mEmoticonPageViews = new ArrayList<View>();
    private List<EmoticonsListener> mEmoticonsListenerListeners;
    private OnEmoticonsPageViewListener mOnEmoticonsPageViewListener;

    public EmoticonsPageView(Context context) {
        this(context, null);
    }

    public EmoticonsPageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mHeight = h;
        EmoticonsPageView.this.post(new Runnable() {
            @Override
            public void run() {
                updateView();
            }
        });
    }

    public void updateView() {
        if (mEmoticonSetBeanList == null) {
            return;
        }

        if (mEmoticonsViewPagerAdapter == null) {
            mEmoticonsViewPagerAdapter = new EmoticonsViewPagerAdapter();
            setAdapter(mEmoticonsViewPagerAdapter);
            setOnPageChangeListener(new OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    if (mOldPagePosition < 0) {
                        mOldPagePosition = 0;
                    }
                    int end = 0;
                    int pagerPosition = 0;
                    for (EmoticonSetBean emoticonSetBean : mEmoticonSetBeanList) {

                        int size = getPageCount(emoticonSetBean);

                        if (end + size > position) {
                            mCurrentViewCount = size;
                            if (mOnEmoticonsPageViewListener != null) {
                                mOnEmoticonsPageViewListener.emoticonsPageViewCountChanged(size);
                            }
                            // 上一页
                            if (mOldPagePosition - end >= size) {
                                if (position - end >= 0) {
                                    if (mOnEmoticonsPageViewListener != null) {
                                        mOnEmoticonsPageViewListener.playTo(position - end);
                                    }
                                }
                                if (mEmoticonsListenerListeners != null && !mEmoticonsListenerListeners.isEmpty()) {
                                    for (EmoticonsListener listener : mEmoticonsListenerListeners) {
                                        listener.onPageChangeTo(pagerPosition);
                                    }
                                }
                                break;
                            }
                            // 下一页
                            if (mOldPagePosition - end < 0) {
                                if (mOnEmoticonsPageViewListener != null) {
                                    mOnEmoticonsPageViewListener.playTo(0);
                                }
                                if (mEmoticonsListenerListeners != null && !mEmoticonsListenerListeners.isEmpty()) {
                                    for (EmoticonsListener listener : mEmoticonsListenerListeners) {
                                        listener.onPageChangeTo(pagerPosition);
                                    }
                                }
                                break;
                            }
                            // 本页切换
                            if (mOnEmoticonsPageViewListener != null) {
                                mOnEmoticonsPageViewListener.playBy(mOldPagePosition - end, position - end);
                            }
                            break;
                        }
                        pagerPosition++;
                        end += size;
                    }
                    mOldPagePosition = position;
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
        }

        int screenWidth = Utils.getDeviceScreenWidth();
        int maxPagerHeight = mHeight;

        mEmoticonPageViews.clear();
        mEmoticonsViewPagerAdapter.notifyDataSetChanged();

        for (EmoticonSetBean bean : mEmoticonSetBeanList) {
            ArrayList<EmoticonBean> emoticonList = bean.getEmoticonList();
            if (emoticonList != null) {
                int emoticonSetSum = emoticonList.size();
                int row = bean.getRow();
                int line = bean.getLine();

                int del = bean.isShowDelBtn() ? 1 : 0;
                int everyPageMaxSum = row * line - del;
                int pageCount = getPageCount(bean);

                mMaxEmoticonSetPageCount = Math.max(mMaxEmoticonSetPageCount, pageCount);

                int start = 0;
                int end = everyPageMaxSum > emoticonSetSum ? emoticonSetSum : everyPageMaxSum;

                RelativeLayout.LayoutParams gridParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                gridParams.addRule(ResizeLayout.CENTER_VERTICAL);

                int itemHeight = Math.min(
                        (screenWidth - (bean.getRow() - 1) * ViewUtils.dp2px(bean.getHorizontalSpacing())) / bean.getRow(),
                        (maxPagerHeight - (bean.getLine() - 1) * ViewUtils.dp2px(bean.getVerticalSpacing())) / bean.getLine()
                );

//                // 计算行距
//                if (bean.getHeight() > 0) {
//                    int verticalspacing = Utils.dip2px(mContext, bean.getVerticalSpacing());
//                    itemHeight = Math.min(itemHeight,Utils.dip2px(mContext, bean.getHeight()));
//                    while (verticalspacing > 0) {
//                        int userdefHeigth = (bean.getLine() - 1) * verticalspacing + Utils.dip2px(mContext, bean.getHeight()) * (bean.getLine());
//                        if (userdefHeigth <= maxPagerHeight) {
//                            bean.setVerticalSpacing(Utils.px2dip(mContext, verticalspacing));
//                            break;
//                        }
//                        bean.setVerticalSpacing(Utils.px2dip(mContext, verticalspacing));
//                        verticalspacing = (int)Math.ceil((float)verticalspacing / 2);
//                    }
//                }

                for (int i = 0; i < pageCount; i++) {
                    RelativeLayout rl = new RelativeLayout(mContext);
                    GridView gridView = new GridView(mContext);
                    gridView.setMotionEventSplittingEnabled(false);
                    gridView.setNumColumns(bean.getRow());
                    gridView.setBackgroundColor(Color.TRANSPARENT);
                    gridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
                    gridView.setCacheColorHint(0);
                    gridView.setHorizontalSpacing(ViewUtils.dp2px(bean.getHorizontalSpacing()));
                    gridView.setVerticalSpacing(ViewUtils.dp2px(bean.getVerticalSpacing()));
                    gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
                    gridView.setGravity(Gravity.CENTER);
                    gridView.setVerticalScrollBarEnabled(false);

                    List<EmoticonBean> list = new ArrayList<EmoticonBean>();
                    for (int j = start; j < end; j++) {
                        list.add(emoticonList.get(j));
                    }

                    // 删除按钮
                    if (bean.isShowDelBtn()) {
                        int count = bean.getLine() * bean.getRow();
                        while (list.size() < count - 1) {
                            list.add(null);
                        }
                        list.add(new EmoticonBean(EmoticonBean.FACE_TYPE_DEL, "drawable://icon_del", null));
                    } else {
                        int count = bean.getLine() * bean.getRow();
                        while (list.size() < count) {
                            list.add(null);
                        }
                    }

                    EmoticonsAdapter adapter = new EmoticonsAdapter(mContext, list);
                    adapter.setHeight(itemHeight, ViewUtils.dp2px(bean.getItemPadding()));
                    gridView.setAdapter(adapter);
                    rl.addView(gridView, gridParams);
                    mEmoticonPageViews.add(rl);
                    adapter.setEmoticonsListener(this);

                    start = everyPageMaxSum + i * everyPageMaxSum;
                    end = everyPageMaxSum + (i + 1) * everyPageMaxSum;
                    if (end >= emoticonSetSum) {
                        end = emoticonSetSum;
                    }
                }
            }
        }
        mEmoticonsViewPagerAdapter.notifyDataSetChanged();

        if (mOnEmoticonsPageViewListener != null) {
            mOnEmoticonsPageViewListener.emoticonsPageViewInitFinish(mCurrentViewCount == -1 ? mMaxEmoticonSetPageCount : mCurrentViewCount);
        }
    }

    public void setPageSelect(int position) {
        if (getAdapter() != null && position >= 0 && position < mEmoticonSetBeanList.size()) {
            mCurrentPage = position;
            int count = 0;
            for (int i = 0; i < position; i++) {
                count += getPageCount(mEmoticonSetBeanList.get(i));
            }
            setCurrentItem(count);
        }
    }

    public int getPageSelected() {
        return mCurrentPage;
    }

    public int getPageCount(EmoticonSetBean emoticonSetBean) {
        int pageCount = 0;
        if (emoticonSetBean != null && emoticonSetBean.getEmoticonList() != null) {
            int del = emoticonSetBean.isShowDelBtn() ? 1 : 0;
            int everyPageMaxSum = emoticonSetBean.getRow() * emoticonSetBean.getLine() - del;
            pageCount = (int) Math.ceil((double) emoticonSetBean.getEmoticonList().size() / everyPageMaxSum);
        }
        return pageCount;
    }

    public void setEmoticonSetBeanList(List<EmoticonSetBean> emoticonSetBeans) {
        mEmoticonSetBeanList = emoticonSetBeans;
    }

    @Override
    public void onItemClick(EmoticonBean bean) {
        if (mEmoticonsListenerListeners != null && !mEmoticonsListenerListeners.isEmpty()) {
            for (EmoticonsListener listener : mEmoticonsListenerListeners) {
                listener.onItemClick(bean);
            }
        }
    }

    @Override
    public void onItemDisplay(View itemView, EmoticonBean bean) {
        if (mEmoticonsListenerListeners != null && !mEmoticonsListenerListeners.isEmpty()) {
            for (EmoticonsListener listener : mEmoticonsListenerListeners) {
                listener.onItemDisplay(itemView, bean);
            }
        }
    }

    @Override
    public void onPageChangeTo(int position) {

    }

    public void addEmoticonsListenerListener(EmoticonsListener listener) {
        if (mEmoticonsListenerListeners == null) {
            mEmoticonsListenerListeners = new ArrayList<EmoticonsListener>();
        }
        mEmoticonsListenerListeners.add(listener);
    }

    public void setEmoticonsListenerListener(EmoticonsListener listener) {
        addEmoticonsListenerListener(listener);
    }

    public void setOnIndicatorListener(OnEmoticonsPageViewListener listener) {
        mOnEmoticonsPageViewListener = listener;
    }

    public interface OnEmoticonsPageViewListener {
        void emoticonsPageViewInitFinish(int count);

        void emoticonsPageViewCountChanged(int count);

        void playTo(int position);

        void playBy(int oldPosition, int newPosition);
    }

    private class EmoticonsViewPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return mEmoticonPageViews.size();
        }

        @Override
        public Object instantiateItem(View arg0, int arg1) {
            ((ViewPager) arg0).addView(mEmoticonPageViews.get(arg1));
            return mEmoticonPageViews.get(arg1);
        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView((View) arg2);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }
    }
}
