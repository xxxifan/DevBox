package com.xxxifan.devbox.library.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.xxxifan.devbox.library.AppPref;
import com.xxxifan.devbox.library.Devbox;
import com.xxxifan.devbox.library.tools.ViewUtils;

public class AutoHeightLayout extends ResizeLayout {

    public static final int KEYBOARD_STATE_NONE = 100;
    public static final int KEYBOARD_STATE_FUNC = 102;
    public static final int KEYBOARD_STATE_BOTH = 103;

    private static final int ID_CHILD = 1;

    protected Context mContext;
    protected int mAutoHeightLayoutId;
    protected int mAutoViewHeight;
    protected View mAutoHeightLayoutView;
    protected int mKeyboardState = KEYBOARD_STATE_NONE;

    private OnResizeListener mListener;

    public AutoHeightLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        mAutoViewHeight = AppPref.getInt(Devbox.PREF_KEYBOARD_HEIGHT, 0);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        int childSum = getChildCount();
        if (getChildCount() > 1) {
            throw new IllegalStateException("can host only one direct child");
        }
        super.addView(child, index, params);

        if (childSum == 0) {
            mAutoHeightLayoutId = child.getId();
            if (mAutoHeightLayoutId < 0) {
                child.setId(ID_CHILD);
                mAutoHeightLayoutId = ID_CHILD;
            }
            RelativeLayout.LayoutParams paramsChild = (RelativeLayout.LayoutParams) child.getLayoutParams();
            paramsChild.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            child.setLayoutParams(paramsChild);
        } else if (childSum == 1) {
            RelativeLayout.LayoutParams paramsChild = (RelativeLayout.LayoutParams) child.getLayoutParams();
            paramsChild.addRule(RelativeLayout.ABOVE, mAutoHeightLayoutId);
            child.setLayoutParams(paramsChild);
        }
    }

    public void setAutoHeightLayoutView(View view) {
        mAutoHeightLayoutView = view;
    }

    public void setAutoViewHeight(final int height) {
        int heightDp = (int) ViewUtils.px2dp(height);
        if (heightDp > 0 && heightDp != mAutoViewHeight) {
            mAutoViewHeight = heightDp;
            AppPref.putInt(Devbox.PREF_KEYBOARD_HEIGHT, mAutoViewHeight);
        }

        if (mAutoHeightLayoutView != null) {
            mAutoHeightLayoutView.setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mAutoHeightLayoutView.getLayoutParams();
            params.height = height;
            mAutoHeightLayoutView.setLayoutParams(params);
        }
    }

    public void hideAutoView() {
        this.post(new Runnable() {
            @Override
            public void run() {
//                ViewUtils.closeKeyboard(mContext);
                setAutoViewHeight(0);
                if (mAutoHeightLayoutView != null) {
                    mAutoHeightLayoutView.setVisibility(View.GONE);
                }
            }
        });
        mKeyboardState = KEYBOARD_STATE_NONE;
    }

    public void showAutoView() {
        if (mAutoHeightLayoutView != null) {
            mAutoHeightLayoutView.setVisibility(VISIBLE);
            setAutoViewHeight(ViewUtils.dp2px(mAutoViewHeight));
        }
        mKeyboardState = mKeyboardState == KEYBOARD_STATE_NONE ? KEYBOARD_STATE_FUNC : KEYBOARD_STATE_BOTH;
    }

    @Override
    public void onKeyboardExpand(final int height) {
        mKeyboardState = KEYBOARD_STATE_BOTH;
        post(new Runnable() {
            @Override
            public void run() {
                setAutoViewHeight(height);
                if (mListener != null) {
                    mListener.onKeyboardExpand(height);
                }
            }
        });
    }

    @Override
    public void onKeyboardCollapse(int height) {
        mKeyboardState = mKeyboardState == KEYBOARD_STATE_BOTH ? KEYBOARD_STATE_FUNC : KEYBOARD_STATE_NONE;
        if (mListener != null) {
            mListener.onKeyboardCollapse(height);
        }
    }

    @Override
    public void onKeyboardHeightChanged(final int height) {
        post(new Runnable() {
            @Override
            public void run() {
                setAutoViewHeight(height);
                if (mListener != null) {
                    mListener.onKeyboardHeightChanged(height);
                }
            }
        });
    }

    public boolean isExpanded() {
        return mKeyboardState != KEYBOARD_STATE_NONE;
    }

    public int getKeyboardState() {
        return mKeyboardState;
    }

    public void setOnResizeListener(OnResizeListener l) {
        mListener = l;
    }

    public interface OnResizeListener {
        /**
         * 软键盘弹起
         */
        void onKeyboardExpand(int height);

        /**
         * 软键盘关闭
         */
        void onKeyboardCollapse(int height);

        /**
         * 软键盘高度改变
         */
        void onKeyboardHeightChanged(int height);
    }

}