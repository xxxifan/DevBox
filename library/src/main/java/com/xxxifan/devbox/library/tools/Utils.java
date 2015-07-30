package com.xxxifan.devbox.library.tools;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import com.xxxifan.devbox.library.AppDelegate;
import com.xxxifan.devbox.library.callbacks.CommandCallback;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Created by xifan on 15-7-19.
 */
public class Utils {
    private static final String CONFIG_SHOW_NAVBAR = "config_showNavigationBar";
    private static final String CONFIG_FORCE_NAVBAR = "dev_force_show_navbar";
    private static final String CONFIG_TOOLBAR_HEIGHT = "status_bar_height";

    private static Handler mWorkerHandler;
    private static boolean sHasTranslucentNavBar;
    private static int sStatusBarHeight;
    private static int sNavBarHeight;
    private static int sScreenHeight;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // find custom settings first: force hardware key by build.prop or cm enabler
            IOUtils.runCmd(new String[]{"getprop", "qemu.hw.mainkeys"}, new CommandCallback() {
                @Override
                public void done(String forceKey, IOException e) {
                    if (e != null) {
                        e.printStackTrace();
                        return;
                    }

                    if (forceKey.equals("0")) {
                        sHasTranslucentNavBar = true;
                        return;
                    }

                    // check cm settings
                    boolean forceCm = Settings.Secure.getInt(AppDelegate.get().getContentResolver(),
                            CONFIG_FORCE_NAVBAR, 0) == 1;

                    // fallback, use common method.
                    sHasTranslucentNavBar = forceCm || readInternalBoolean(CONFIG_SHOW_NAVBAR,
                            AppDelegate.get().getResources(), !ViewConfiguration.get(AppDelegate.get())
                                    .hasPermanentMenuKey());
                }
            });
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

    public static File getTempFile(String filename) {
        return new File(AppDelegate.get().getCacheDir(), filename);
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

    public static int getDeviceScreenHeight() {
        if (sScreenHeight == 0) {
            Display display = ((WindowManager) AppDelegate.get().getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                ((WindowManager) AppDelegate.get().getSystemService(Context.WINDOW_SERVICE))
                        .getDefaultDisplay().getRealMetrics(metrics);
                sScreenHeight = metrics.heightPixels;
            } else {
                try {
                    Method method = display.getClass().getMethod("getRealMetrics", DisplayMetrics.class);
                    method.invoke(display, metrics);
                    sScreenHeight = metrics.heightPixels;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return sScreenHeight;
    }

    public static int getNavBarHeight() {
        if (sNavBarHeight == 0) {
            int deviceScreenHeight = getDeviceScreenHeight();
            int displayHeight = AppDelegate.get().getResources().getDisplayMetrics().heightPixels;
            sNavBarHeight = deviceScreenHeight - displayHeight;
            if (sNavBarHeight <= 0) {
                sNavBarHeight = ViewUtils.dp2px(48);
            }
        }
        return sNavBarHeight;
    }

    /**
     * Get app package info.
     */
    public static PackageInfo getPackageInfo() throws PackageManager.NameNotFoundException {
        PackageManager manager = AppDelegate.get().getPackageManager();
        return manager.getPackageInfo(AppDelegate.get().getPackageName(), 0);
    }

    public static long getVersionCode() {
        try {
            return getPackageInfo().versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return Long.MAX_VALUE;
        }
    }

    public static String getVersionName() {
        try {
            return getPackageInfo().versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void postTask(Runnable runnable) {
        HandlerThread handlerThread = AppDelegate.getWorkerThread();
        if (handlerThread.getState() == Thread.State.NEW) {
            handlerThread.start();
        }
        if (mWorkerHandler == null) {
            mWorkerHandler = new Handler(handlerThread.getLooper());
        }
        mWorkerHandler.post(runnable);
    }

}
