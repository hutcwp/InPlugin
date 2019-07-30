/*
 * Copyright 2015-present wequick.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.hutcwp.cow.luancher;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Handler;
import android.view.Window;
import com.hutcwp.cow.util.ReflectAccelerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * This class launch the plugin activity by it's class name.
 *
 * <p>This class resolve the bundle who's <tt>pkg</tt> is specified as
 * <i>"*.app.*"</i> or <i>*.lib.*</i> in <tt>bundle.json</tt>.
 *
 * <ul>
 * <li>The <i>app</i> plugin contains some activities usually, while launching,
 * takes the bundle's <tt>uri</tt> as default activity. the other activities
 * can be specified by the bundle's <tt>rules</tt>.</li>
 *
 * <li>The <i>lib</i> plugin which can be included by <i>app</i> plugin
 * consists exclusively of global methods that operate on your product services.</li>
 * </ul>
 *
 * @see
 */
public class ApkPluginLauncher2 extends PluginLauncher {

    //    private static final String PACKAGE_NAME = Small.class.getPackage().getName();
//    private static final String STUB_ACTIVITY_PREFIX = PACKAGE_NAME + ".A";
//    private static final String STUB_ACTIVITY_TRANSLUCENT = STUB_ACTIVITY_PREFIX + '1';
    private static final String TAG = "ApkPluginLauncher2";

    //    private static ThreadBlocker mThreadBlocker = new ThreadBlocker(ThreadBlocker.DEFAULT_TIME_OUT_MS);
    private static Handler mMainThreadHandler;

    private static Map<String, Integer> sErrorActivityRecords = new HashMap<>();

    //    private static ConcurrentHashMap<String, PluginDexLoader.LoadedApk> sLoadedApks;
    private static ConcurrentHashMap<String, ActivityInfo> sLoadedActivities = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, List<IntentFilter>> sLoadedIntentFilters;

    private static List<String> sRespathList = new ArrayList<>();
    private static Intent mFirstIntent;

    private static final int FIRSTACTIVITY_CHECK_COUNT_MAX = 4;
    private static int firstActivityCheckCount = 0;

    protected static Instrumentation sHostInstrumentation;
    private static Instrumentation sBundleInstrumentation;


    @Override
    public void preSetUp(Application context) {
        super.preSetUp(context);
        if (sHostInstrumentation == null) {
            try {
//                sHostInstrumentation = ReflectAccelerator.getHostInstrumentation();
//                Instrumentation wrapper = new InstrumentationWrapper(sHostInstrumentation);
//                ReflectAccelerator.setHostInstrumentation(wrapper);
//
//                if (!sHostInstrumentation.getClass().getName().equals("android.app.Instrumentation")) {
//                    sBundleInstrumentation = wrapper; // record for later replacement
//                }
//
//                ReflectAccelerator.setActivityThreadHandlerCallback(new ActivityThreadHandlerCallback());
            } catch (Exception ignored) {
                ignored.printStackTrace();
                // Usually, cannot reach here
            }
        }
    }

    @Override
    public void setUp(Context context) {
        super.setUp(context);
    }

    @Override
    public void postSetUp() {
        super.postSetUp();

//        if (sLoadedApks == null) {
//            Logging.error(TAG, "Could not find any APK bundles!");
//            return;
//        }
//
//        final Collection<PluginDexLoader.LoadedApk> apks = sLoadedApks.values();
//
//        // Merge all the resources in bundles and replace the host one
//        final Application app = Small.getContext();
//        String path = app.getPackageResourcePath();
//        if (!sRespathList.contains(path)) {
//            sRespathList.add(path);
//        }
//
//        List<PluginDexLoader.LoadedApk> resApkList = new ArrayList<>(apks.size());
//        for (PluginDexLoader.LoadedApk apk : apks) {
//            if (apk.nonResources) continue; // ignores the empty entry to fix #62
//            if (!sRespathList.contains(apk.path)) {
//                sRespathList.add(apk.path);
//                resApkList.add(apk);
//            }
//        }
//        final String[] paths = new String[sRespathList.size()];
//        for (int i = 0; i < sRespathList.size(); i++) {
//            paths[i] = sRespathList.get(i);
//        }
//
//        final List<PluginDexLoader.LoadedApk> tempApkList = resApkList;
//
//        Runnable mergeResourceTask = (new Runnable() {
//            @Override
//            public void run() {
//                long before = System.currentTimeMillis();
//                try {
//                    ReflectAccelerator.mergeResources(app, paths);
//                } catch (Exception e) {
//                    for (PluginDexLoader.LoadedApk loadedApk : tempApkList) { // for every possible apk
//                        StatisticsBase.record(StatisticsBase.Const.EVENT_LOAD_PLUGIN_FAILURE + loadedApk.packageName,
//                        "resources_merge_failed", Log.getStackTraceString(e));
//                    }
//                }
//                long after = System.currentTimeMillis();
//                Logging.info(TAG, "merge resource take time: %d", after - before);
//            }
//        });
//
//        // Merge all the dex into host's class loader
//        final ClassLoader cl = app.getClassLoader();
//
//        Runnable installDexTask = new Runnable() {
//            @Override
//            public void run() {
//                long before = System.currentTimeMillis();
//                try {
//                    PluginDexLoader.installPlugins(cl, apks);
//                } catch (Exception e) {
//                    Logging.error(TAG, "exception occurred when installing dex", e);
//                    StatisticsBase.record(StatisticsBase.Const.EVENT_LOAD_PLUGIN_FAILURE, "dex_install_failed",
//                    Log.getStackTraceString(e));
//                }
//
//                long after = System.currentTimeMillis();
//                Logging.info(TAG, "merge dex take time %d", after - before);
//            }
//        };
//
//        List<Runnable> uiTasks = new ArrayList<>();
//        List<Runnable> tasks = new ArrayList<>();
//        uiTasks.add(mergeResourceTask);
//        tasks.add(installDexTask);
//
//        new MultiThreadRun(tasks).run();
//        new MultiThreadRun(uiTasks).runOnUiThread(Small.getContext());
//
//        long before = System.currentTimeMillis();
//        // Expand the native library directories for host class loader if plugin has any JNIs. (#79)
//        List<File> libPathList = new ArrayList<File>();
//        for (PluginDexLoader.LoadedApk apk : apks) {
//            if (apk.libraryPath != null) {
//                libPathList.add(apk.libraryPath);
//            }
//        }
//        if (libPathList.size() > 0) {
//            // below android 4.1
//            boolean push = false;
//            if (Build.VERSION.SDK_INT <= 16) {
//                Set<String> hostSoPath = new HashSet<>();
//                File hostLibDir = new File(PluginInstaller.getDefaultBuiltInPluginDirectory());
//                File[] files = hostLibDir.listFiles();
//                if (files != null) {
//                    for (File so : files) {
//                        Logging.warn(TAG, "host so %s", so.getAbsolutePath());
//                        hostSoPath.add(so.getName());
//                    }
//                }
//                for (File dir : libPathList) {
//                    files = dir.listFiles();
//                    if (files != null) {
//                        for (File so : files) {
//                            Logging.warn(TAG, "plugin so %s", so.getAbsolutePath());
//                            if (hostSoPath.contains(so.getName())) {
//                                Logging.warn(TAG, "found host contains plugin so %s", so.getName());
//                                push = true;
//                                break;
//                            }
//                        }
//                    }
//                    if (push) {
//                        break;
//                    }
//                }
//            }
//            ReflectAccelerator.expandNativeLibraryDirectories(cl, libPathList, push);
//        }
//
//        long after = System.currentTimeMillis();
//        Logging.info(TAG, "merge so22 take time : %d", after - before);
//
//        // Free temporary variables
//        sLoadedApks = null;
    }

//    @Override
//    protected String[] getSupportingTypes() {
//        return new String[]{"app", "lib"};
//    }
//
//    @Override
//    public void loadPlugin(PluginRecord plugin) {
//        String packageName = plugin.packageName();
//
//        PluginParser parser = plugin.getParser();
//
//        PackageInfo pluginInfo = null;
//        if (parser != null) {
//            //parser.collectActivities();
//            Logging.info(TAG, "collectActivities %s", plugin.packageName());
//            pluginInfo = parser.getPackageInfo();
//        }
//
//        // Load the bundle
//        if (sLoadedApks == null) sLoadedApks = new ConcurrentHashMap<String, PluginDexLoader.LoadedApk>();
//        PluginDexLoader.LoadedApk apk = sLoadedApks.get(packageName);
//        if (apk == null) {
//            apk = new PluginDexLoader.LoadedApk();
//            apk.id = plugin.id();
//            apk.version = plugin.version();
//            apk.packageName = packageName;
//            apk.path = plugin.apkFile().getPath();
//            apk.nonResources = parser != null ? parser.isNonResources() : true;
//            if (pluginInfo != null) {
//                if (pluginInfo.applicationInfo != null) {
//                    apk.applicationName = pluginInfo.applicationInfo.className;
//                }
//            }
//            apk.packagePath = plugin.apkFile().getParentFile();
//
//            String libDir = parser != null ? parser.getLibraryDirectory() : null;
//            if (libDir != null) {
//                apk.libraryPath = new File(apk.packagePath, libDir);
//            }
//            sLoadedApks.put(packageName, apk);
//        }
//
//        if (pluginInfo != null) {
//            if (pluginInfo.activities == null) {
//                return;
//            }
//        }
//
//        // Record activities for intent redirection
//        if (sLoadedActivities == null) sLoadedActivities = new ConcurrentHashMap<String, ActivityInfo>();
//        if (pluginInfo != null) {
//            for (ActivityInfo ai : pluginInfo.activities) {
//                sLoadedActivities.put(ai.name, ai);
//            }
//        }
//
//        // Record intent-filters for implicit action
//        if (parser != null) {
//            ConcurrentHashMap<String, List<IntentFilter>> filters = parser.getIntentFilters();
//            if (filters != null) {
//                if (sLoadedIntentFilters == null) {
//                    sLoadedIntentFilters = new ConcurrentHashMap<String, List<IntentFilter>>();
//                }
//                sLoadedIntentFilters.putAll(filters);
//            }
//        }
//    }

    /**
     * Apply plugin activity info with plugin's AndroidManifest.xml
     *
     * @param activity
     * @param ai
     */
    private static void applyActivityInfo(Activity activity, ActivityInfo ai) {
        // Apply window attributes
        if (Build.VERSION.SDK_INT >= 28) {
            ReflectAccelerator.resetResourcesAndTheme(activity, ai.getThemeResource());
        }

        Window window = activity.getWindow();
        window.setSoftInputMode(ai.softInputMode);
        activity.setRequestedOrientation(ai.screenOrientation);
    }

//    public static void syncRunOnUiThread(final Runnable runnable) {
//        if (mMainThreadHandler == null) {
//            mMainThreadHandler = new Handler(Small.getContext().getMainLooper());
//        }
//
//        if (Looper.myLooper() != Looper.getMainLooper()) {
//            mThreadBlocker.preBlock();
//            mMainThreadHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    runnable.run();
//                    mThreadBlocker.unblock();
//                }
//            });
//            mThreadBlocker.waitForUnblock();
//        } else {
//            runnable.run();
//        }
//    }
//
//    public static void setFirstActivityMonitor(FirstActivityMonitor firstActivityMonitor) {
//        sFirstActivityMonitor = firstActivityMonitor;
//    }
//
//    public static void setNewActivityMonitor(NewActivityMonitor newActivityMonitor) {
//        sNewActivityMonitor = newActivityMonitor;
//    }

    public static void setFirstActivityIntent(Intent intent) {
        mFirstIntent = intent;
    }
}
