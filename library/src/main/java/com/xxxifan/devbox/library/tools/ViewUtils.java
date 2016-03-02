package com.xxxifan.devbox.library.tools;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.StringRes;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.xxxifan.devbox.library.Devbox;
import com.xxxifan.devbox.library.R;

/**
 * Created by xifan on 15-7-16.
 */
public class ViewUtils {
    private static float sDensity = 0;

    protected ViewUtils() {
    }

    public static int dp2px(float dp) {
        return (int) (dp * getDensity() + 0.5f);
    }

    public static float px2dp(int px) {
        return px / getDensity() + 0.5f;
    }

    public static float getDensity() {
        if (sDensity == 0) {
            try {
                sDensity = Devbox.getAppDelegate().getResources().getDisplayMetrics().density;
            } catch (Exception e) {
                sDensity = 2f;
            }
        }
        return sDensity;
    }

    public static void closeKeyboard(Context context) {
        if (context != null) {
            View focus = ((Activity) context).getCurrentFocus();
            if (focus != null && focus instanceof EditText) {
                closeKeyboard((EditText) focus);
            }
        }
    }

    /**
     * @param editor one of EditText
     */
    public static void closeKeyboard(EditText editor) {
        ((InputMethodManager) editor.getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(editor.getWindowToken(), 0);
    }

    public static void showKeyboard(EditText editor) {
        ((InputMethodManager) editor.getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                .toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        editor.requestFocus();
    }

    public static void addTextDelLine(TextView textView) {
        textView.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
    }

    public static MaterialDialog getLoadingDialog(Context context) {
        return getLoadingDialog(context, null);
    }

    public static MaterialDialog getLoadingDialog(Context context, String msg) {
        String str = msg;
        if (msg == null) {
            str = context.getString(R.string.msg_loading);
        }
        return new MaterialDialog.Builder(context)
                .content(str)
                .progress(true, 0)
                .cancelable(false)
                .build();
    }

    public static void dismissDialog(Dialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public static MaterialDialog.Builder getSimpleDialogBuilder(Context context) {
        return new MaterialDialog.Builder(context)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel);
    }

    public static void showToast(@StringRes int resId, int duration) {
        Toast.makeText(Devbox.getAppDelegate(), resId, duration).show();
    }

    public static void showToast(String toastStr, int duration) {
        Toast.makeText(Devbox.getAppDelegate(), toastStr, duration).show();
    }
}
