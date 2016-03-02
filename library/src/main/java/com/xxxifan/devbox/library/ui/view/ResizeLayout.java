package com.xxxifan.devbox.library.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import java.util.ArrayList;

public abstract class ResizeLayout extends RelativeLayout {

    private static final String TAG = "ResizeLayout";
    private int mMaxParentHeight = 0;
    private ArrayList<Integer> heightList = new ArrayList<Integer>();

    public ResizeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (mMaxParentHeight == 0) {
            mMaxParentHeight = h;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measureHeight = measureHeight(heightMeasureSpec);
        heightList.add(measureHeight);
        if (mMaxParentHeight != 0) {
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            int expandSpec = MeasureSpec.makeMeasureSpec(mMaxParentHeight, heightMode);
            super.onMeasure(widthMeasureSpec, expandSpec);
            return;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (heightList.size() >= 2) {
            int oldh = heightList.get(0);
            int newh = heightList.get(heightList.size() - 1);
            int softHeight = mMaxParentHeight - newh;
             /*
             newh         oldh
             500            max         -> 弹起                softKeyboard = max - 500
             600            500         -> 缩小                softKeyboard = max - 600
             500            600         -> 拉伸               softKeyboard = max - 500
             max           500         -> 关闭               softKeyboard = 0
             */
            /**
             * 弹出软键盘
             */
            if (oldh == mMaxParentHeight) {
                onKeyboardExpand(softHeight);
            }
            /**
             * 隐藏软键盘
             */
            else if (newh == mMaxParentHeight) {
                onKeyboardCollapse(softHeight);
            }
            /**
             * 调整软键盘高度
             */
            else {
                onKeyboardHeightChanged(softHeight);
            }
            heightList.clear();
        } else {
            heightList.clear();
        }
    }

    private int measureHeight(int pHeightMeasureSpec) {
        int result = 0;
        int heightMode = MeasureSpec.getMode(pHeightMeasureSpec);
        int heightSize = MeasureSpec.getSize(pHeightMeasureSpec);
        switch (heightMode) {
            case MeasureSpec.AT_MOST:
            case MeasureSpec.EXACTLY:
                result = heightSize;
                break;
        }
        return result;
    }

    /**
     * 软键盘弹起
     */
    protected abstract void onKeyboardExpand(int height);

    /**
     * 软键盘关闭
     */
    protected abstract void onKeyboardCollapse(int height);

    /**
     * 软键盘高度改变
     */
    protected abstract void onKeyboardHeightChanged(int height);

}