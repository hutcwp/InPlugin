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

package com.hutcwp.small.luancher;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import com.hutcwp.small.Small;
import com.hutcwp.small.hook.AMSHookHelper;
import com.hutcwp.small.internal.ActivityThreadHandlerCallback;
import com.hutcwp.small.internal.InstrumentationWrapper;
import com.hutcwp.small.plugin.PluginRecord;
import com.hutcwp.small.util.PluginDexLoader;
import com.hutcwp.small.util.PluginUtil;
import com.hutcwp.small.util.ReflectAccelerator;

import java.util.HashSet;
import java.util.List;
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
public class ApkPluginLauncher extends PluginLauncher {

    private static final String TAG = "ApkPluginLauncher";


    private static Instrumentation sHostInstrumentation;
    private static Instrumentation sBundleInstrumentation;

    private static ConcurrentHashMap<String, PluginDexLoader.LoadedApk> sLoadedApks; //key:packageName

    private static ConcurrentHashMap<String, ActivityInfo> sLoadedActivities = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<String, List<IntentFilter>> sLoadedIntentFilters;

    public static ConcurrentHashMap<String, ActivityInfo> getsLoadedActivities() {
        return sLoadedActivities;
    }

    public static ConcurrentHashMap<String, List<IntentFilter>> getsLoadedIntentFilters() {
        return sLoadedIntentFilters;
    }

    private static HashSet<String> sActivityClasses;

    public static boolean containsActivity(String name) {
        return sActivityClasses != null && sActivityClasses.contains(name);
    }


    @Override
    public void preSetUp(Application context) {
        super.preSetUp(context);
        if (sHostInstrumentation == null) {
            try {
                sHostInstrumentation = ReflectAccelerator.getHostInstrumentation();
                Instrumentation wrapper = new InstrumentationWrapper(sHostInstrumentation);
                ReflectAccelerator.setHostInstrumentation(wrapper);

                if (!sHostInstrumentation.getClass().getName().equals("android.app.Instrumentation")) {
                    sBundleInstrumentation = wrapper; // record for later replacement
                }

                AMSHookHelper.hookAMN();
                ReflectAccelerator.setActivityThreadHandlerCallback(new ActivityThreadHandlerCallback());
            } catch (Exception e) {
                Log.e(TAG, "preSetUp : hook error.", e);
                // Usually, cannot reach here
            }
        }
    }

    @Override
    public void setUp(Context context) {
        super.setUp(context);
        try {
            // Read the registered classes in host's manifest file
            PackageInfo pi;
            try {
                pi = context.getPackageManager().getPackageInfo(
                        context.getPackageName(), PackageManager.GET_ACTIVITIES);
            } catch (PackageManager.NameNotFoundException ignored) {
                // Never reach
                return;
            }
            ActivityInfo[] as = pi.activities;
            if (as != null) {
                sActivityClasses = new HashSet<String>();
                for (ActivityInfo ai : as) {
                    sActivityClasses.add(ai.name);
                }
            }
        } catch (Throwable throwable) {
            sActivityClasses = new HashSet<>();
        }
    }

    @Override
    public void postSetUp() {
        super.postSetUp();
        mergeDexAndResource();
    }

    // todo 还需要支持apk,so,jar等类型
    @Override
    public void loadPlugin(PluginRecord pluginRecord) {
        if (sLoadedApks == null) {
            sLoadedApks = new ConcurrentHashMap<>();
        }

        if (pluginRecord.getPackageInfo() == null) {
            Log.e(TAG, "pluginRecord.getPackageInfo() == null");
            return;
        }

        PluginDexLoader.LoadedApk apk = sLoadedApks.get(pluginRecord.getPackageInfo().packageName);
        if (apk == null) {
            apk = new PluginDexLoader.LoadedApk();
            apk.id = pluginRecord.getPluginInfo().id;
            apk.version = pluginRecord.getPluginInfo().version;
            apk.packageName = pluginRecord.getPackageInfo().packageName;
            String apkFileName = pluginRecord.getPluginInfo().apkFileName;
            apk.path = PluginUtil.getPluginPath(apkFileName);
            apk.apkFile = Small.mBaseContext.getFileStreamPath(apkFileName);
            apk.dexFile = Small.mBaseContext.getFileStreamPath(apkFileName.replace(".apk", ".dex"));
            sLoadedApks.put(pluginRecord.getPackageInfo().packageName, apk);
        }

        // Record activities for intent redirection
        if (sLoadedActivities == null) sLoadedActivities = new ConcurrentHashMap<>();
        if (pluginRecord.getPackageInfo() != null) {
            for (ActivityInfo ai : pluginRecord.getPluginParser().getPackageInfo().activities) {
                sLoadedActivities.put(ai.name, ai);
            }
        }

        // Record intent-filters for implicit action
        if (pluginRecord.getPluginParser() != null) {
            ConcurrentHashMap<String, List<IntentFilter>> filters = pluginRecord.getPluginParser().getIntentFilters();
            if (filters != null) {
                if (sLoadedIntentFilters == null) {
                    sLoadedIntentFilters = new ConcurrentHashMap<String, List<IntentFilter>>();
                }
                sLoadedIntentFilters.putAll(filters);
            }
        }
    }

    private void mergeDexAndResource() {
        if (sLoadedApks == null) {
            return;
        }

        for (PluginDexLoader.LoadedApk apk : sLoadedApks.values()) {
            if (apk != null) {
                try {
                    PluginDexLoader.mergeDexs(apk.apkFile, apk.dexFile);
                    PluginDexLoader.updateResource(apk.path);
                } catch (Exception e) {
                    Log.e(TAG, "mergeDexAndResource error ", e);
                }
            }
        }
    }

    @Override
    public boolean resolvePlugin(PluginRecord plugin) {
        return super.resolvePlugin(plugin);
    }

    @Override
    public boolean preloadPlugin(PluginRecord plugin) {
        return super.preloadPlugin(plugin);
    }

}
