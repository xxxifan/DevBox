package com.xxxifan.devbox.library.tools;

import android.content.SharedPreferences;

import com.xxxifan.devbox.library.AppPref;

/**
 * Idea from https://github.com/drakeet/Meizhi/blob/master/app/src/main/java/me/drakeet/meizhi/util/Once.java
 * Created by xifan on 15-8-23.
 */
public class Once {

    private Once() {
    }

    /**
     * @return isOnce
     */
    public static boolean check(String key) {
        SharedPreferences pref = AppPref.getPrefs("once");
        if (!pref.getBoolean(key, false)) {
            pref.edit().putBoolean(key, true).apply();
            return true;
        }
        return false;
    }

    public static void reset(String key) {
        SharedPreferences pref = AppPref.getPrefs("once");
        pref.edit().putBoolean(key, false).apply();
    }
}
