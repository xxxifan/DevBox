package com.xxxifan.devbox.library;

import android.app.Application;
import android.os.HandlerThread;

/**
 * Created by xifan on 15-7-16.
 */
public class AppDelegate {
    private static Application sApplication;
    private static HandlerThread sWorkerThread;

    public static void install(Application application) {
        sApplication = application;
    }

    public static Application get() {
        if (sApplication == null) {
            throw new IllegalStateException("Application instance is null, please check you have " +
                    "correct config");
        }
        return sApplication;
    }

    public static HandlerThread getWorkerThread() {
        if (sWorkerThread == null) {
            sWorkerThread = new HandlerThread("DevBoxTask", 3);
        }
        return sWorkerThread;
    }

}
