package com.xxxifan.devbox.library.tools;

import com.xxxifan.devbox.library.AppDelegate;

import java.io.File;

/**
 * Created by xifan on 15-7-19.
 */
public class Utils {
    public static File getCacheDir() {
        return AppDelegate.get().getCacheDir();
    }
}
