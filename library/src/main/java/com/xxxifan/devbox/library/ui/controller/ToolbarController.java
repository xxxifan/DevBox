package com.xxxifan.devbox.library.ui.controller;

import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.view.View;
import android.widget.TextView;

/**
 * Created by xifan on 15-11-24.
 */
public abstract class ToolbarController extends UiController {

    public ToolbarController(View toolbarStub) {
        super(toolbarStub);
    }

    @Override
    protected void initView(View view) {
    }

    public abstract void setTitleText(CharSequence title);

    public abstract void setTitleColor(@ColorInt int color);

    public abstract void setTitleTextSize(int size);

    public abstract void onBackButtonClick();

    public abstract void setBackButton(Drawable icon, View.OnClickListener listener);

    public abstract void setRightButton(Drawable icon, View.OnClickListener listener);

    public abstract void setRightView(View view, View.OnClickListener listener);

    public abstract void setRightButtonVisibility(int visibility);

    public abstract void setBackButtonVisibility(int visibility);

    public abstract View getLeftView();

    public abstract View getCenterView();

    public abstract TextView getTitleView();

    public abstract void setTitleView(View titleView);

    public abstract View getRightView();

    public interface Handler {
        ToolbarController getController(View toolbarView);
    }
}
