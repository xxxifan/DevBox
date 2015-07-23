package com.xxxifan.devbox.library.tools;

import android.content.res.Resources;
import android.os.Build;
import android.os.Environment;
import android.view.ViewConfiguration;

import com.xxxifan.devbox.library.AppDelegate;

import java.io.File;

/**
 * Created by xifan on 15-7-19.
 */
public class Utils {
    private static final String CONFIG_SHOW_NAVBAR = "config_showNavigationBar";
    private static final String CONFIG_TOOLBAR_HEIGHT = "status_bar_height";
    private static final String CONFIG_NAVBAR_HEIGHT = "navigation_bar_height";

    private static boolean sHasTranslucentNavBar;
    private static int sStatusBarHeight;
    private static int sNavBarHeight;

    static {
        Resources res = AppDelegate.get().getResources();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            sHasTranslucentNavBar = readInternalBoolean(CONFIG_SHOW_NAVBAR, res,
                    !ViewConfiguration.get(AppDelegate.get()).hasPermanentMenuKey());
        }
    }

    private static int readInternalDimen(String key, Resources res, int fallback) {
        int resourceId = res.getIdentifier(key, "dimen", "android");
        return resourceId > 0 ? res.getDimensionPixelSize(resourceId) : fallback;
    }

    private static boolean readInternalBoolean(String key, Resources res, boolean fallback) {
        int resourceId = res.getIdentifier(key, "bool", "android");
        return resourceId != 0 ? res.getBoolean(resourceId) : fallback;
    }

    public static File getCacheDir() {
        return AppDelegate.get().getCacheDir();
    }

    public static boolean hasMediaMounted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static boolean hasTranslucentNavBar() {
        return sHasTranslucentNavBar;
    }

    public static boolean hasTranslucentBar() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static int getSystemBarHeight() {
        if (sStatusBarHeight == 0) {
            sStatusBarHeight = readInternalDimen(CONFIG_TOOLBAR_HEIGHT, AppDelegate.get().getResources(),
                    ViewUtils.dp2px(24));
        }
        return sStatusBarHeight;
    }

    public static int getNavBarHeight() {
        if (sNavBarHeight == 0) {
            sNavBarHeight = readInternalDimen(CONFIG_NAVBAR_HEIGHT, AppDelegate.get().getResources(),
                    ViewUtils.dp2px(48));
        }
        return sNavBarHeight;
    }

}
