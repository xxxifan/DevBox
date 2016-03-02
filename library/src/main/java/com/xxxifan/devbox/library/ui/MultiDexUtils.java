package com.xxxifan.devbox.library.ui;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.multidex.MultiDex;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import dalvik.system.DexFile;

/**
 * Created by xifan on 16-1-11.
 */
public class MultiDexUtils {

    private static final String KEY_DEX2_SHA1 = "dex2_sha1";
    private static final String EXTRACTED_NAME_EXT = ".classes";
    private static final String EXTRACTED_SUFFIX = ".zip";
    private static final String SECONDARY_FOLDER_NAME = "code_cache" + File.separator + "secondary-dexes";
    private static final String PREFS_FILE = "multidex.version";
    private static final String KEY_DEX_NUMBER = "dex.number";

    /**
     * check this application whether need to do dexopt or quickstart.
     *
     * @param baseContext base context from {@link android.app.Application#attachBaseContext(Context)}
     * @param loaderClass loader activity to do dexopt which can avoid ANR.
     */
    public static void attachDex(Context baseContext, Class<?> loaderClass) {
        if (needDexOpt(baseContext)) {
            waitForDexOpt(baseContext, loaderClass);
        }

        MultiDex.install(baseContext);
    }

    public static boolean needDexOpt(Context context) {
        String flag = get2ndDexSHA1(context);
        String saveValue = getMultiDexPreferences(context).getString(KEY_DEX2_SHA1, "");
        return !TextUtils.isEmpty(flag) && !flag.equals(saveValue);
    }

    public static void waitForDexOpt(Context base, Class<?> loaderActivity) {
        Intent intent = new Intent(base, loaderActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        base.startActivity(intent);
        long startWait = System.currentTimeMillis();
        long waitTime = 10 * 1000;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
            waitTime = 20 * 1000;//实测发现某些场景下有些2.3版本有可能10s都不能完成optdex
        }
        while (needDexOpt(base)) {
            try {
                long nowWait = System.currentTimeMillis() - startWait;
                if (nowWait >= waitTime) {
                    return;
                }
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // optDex finish
    public static void installFinish(Context context) {
        getMultiDexPreferences(context).edit().putString(KEY_DEX2_SHA1, get2ndDexSHA1(context)).commit();
    }

    public static String getCurProcessName(Context context) {
        try {
            int pid = android.os.Process.myPid();
            ActivityManager activityManager =
                    (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfoList =
                    activityManager.getRunningAppProcesses();

            ActivityManager.RunningAppProcessInfo appProcess;
            for (int i = 0, s = runningAppProcessInfoList.size(); i < s; i++) {
                appProcess = runningAppProcessInfoList.get(i);
                if (appProcess.pid == pid) {
                    return appProcess.processName;
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    public static SharedPreferences getMultiDexPreferences(Context context) {
        return context.getSharedPreferences(PREFS_FILE, Context.MODE_MULTI_PROCESS);
    }

    /**
     * Get classes.dex file signature
     *
     * @param context
     * @return
     */
    private static String get2ndDexSHA1(Context context) {
        ApplicationInfo ai = context.getApplicationInfo();
        String source = ai.sourceDir;
        try {
            JarFile jar = new JarFile(source);
            Manifest mf = jar.getManifest();
            Map<String, Attributes> map = mf.getEntries();
            Attributes a = map.get("classes2.dex");
            return a.getValue("SHA1-Digest");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * get all the dex path
     *
     * @param context the application context
     * @return all the dex path
     * @throws PackageManager.NameNotFoundException
     * @throws IOException
     */
    private static List<String> getSourcePaths(Context context) throws PackageManager.NameNotFoundException, IOException {
        final ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
        final File sourceApk = new File(applicationInfo.sourceDir);
        final File dexDir = new File(applicationInfo.dataDir, SECONDARY_FOLDER_NAME);

        final List<String> sourcePaths = new ArrayList<>();
        sourcePaths.add(applicationInfo.sourceDir); //add the default apk path

        //the prefix of extracted file, ie: test.classes
        final String extractedFilePrefix = sourceApk.getName() + EXTRACTED_NAME_EXT;
        //the total dex numbers
        final int totalDexNumber = getMultiDexPreferences(context).getInt(KEY_DEX_NUMBER, 1);

        for (int secondaryNumber = 2; secondaryNumber <= totalDexNumber; secondaryNumber++) {
            //for each dex file, ie: test.classes2.zip, test.classes3.zip...
            final String fileName = extractedFilePrefix + secondaryNumber + EXTRACTED_SUFFIX;
            final File extractedFile = new File(dexDir, fileName);
            if (extractedFile.isFile()) {
                sourcePaths.add(extractedFile.getAbsolutePath());
                //we ignore the verify zip part
            } else {
                throw new IOException("Missing extracted secondary dex file '" +
                        extractedFile.getPath() + "'");
            }
        }

        return sourcePaths;
    }

    /**
     * get all the external classes name in "classes2.dex", "classes3.dex" ....
     *
     * @param context the application context
     * @return all the classes name in the external dex
     * @throws PackageManager.NameNotFoundException
     * @throws IOException
     */
    public static List<String> getExternalDexClasses(Context context) throws PackageManager.NameNotFoundException, IOException {
        final List<String> paths = getSourcePaths(context);
        if (paths.size() <= 1) {
            // no external dex
            return null;
        }
        // the first element is the main dex, remove it.
        paths.remove(0);
        final List<String> classNames = new ArrayList<>();
        for (String path : paths) {
            try {
                DexFile dexfile = null;
                if (path.endsWith(EXTRACTED_SUFFIX)) {
                    //NOT use new DexFile(path), because it will throw "permission error in /data/dalvik-cache"
                    dexfile = DexFile.loadDex(path, path + ".tmp", 0);
                } else {
                    dexfile = new DexFile(path);
                }
                final Enumeration<String> dexEntries = dexfile.entries();
                while (dexEntries.hasMoreElements()) {
                    classNames.add(dexEntries.nextElement());
                }
            } catch (IOException e) {
                throw new IOException("Error at loading dex file '" +
                        path + "'");
            }
        }
        return classNames;
    }

    /**
     * Get all loaded external classes name in "classes2.dex", "classes3.dex" ....
     *
     * @param context
     * @return get all loaded external classes
     */
    public static List<String> getLoadedExternalDexClasses(Context context) {
        try {
            final List<String> externalDexClasses = getExternalDexClasses(context);
            if (externalDexClasses != null && !externalDexClasses.isEmpty()) {
                final ArrayList<String> classList = new ArrayList<>();
                final java.lang.reflect.Method m = ClassLoader.class.getDeclaredMethod("findLoadedClass", String.class);
                m.setAccessible(true);
                final ClassLoader cl = context.getClassLoader();
                for (String clazz : externalDexClasses) {
                    if (m.invoke(cl, clazz) != null) {
                        classList.add(clazz.replaceAll("\\.", "/").replaceAll("$", ".class"));
                    }
                }
                return classList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
