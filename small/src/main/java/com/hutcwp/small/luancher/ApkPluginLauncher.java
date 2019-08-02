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
import android.util.Log;
import com.hutcwp.small.hook.AMSHookHelper;
import com.hutcwp.small.internal.ActivityThreadHandlerCallback;
import com.hutcwp.small.internal.InstrumentationWrapper;
import com.hutcwp.small.plugin.PluginManager;
import com.hutcwp.small.plugin.PluginRecord;
import com.hutcwp.small.util.PluginDexLoader;
import com.hutcwp.small.util.PluginUtil;
import com.hutcwp.small.util.ReflectAccelerator;
import com.hutcwp.small.util.Utils;

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

    private static final int FIRSTACTIVITY_CHECK_COUNT_MAX = 4;
    private static int firstActivityCheckCount = 0;

    private static Instrumentation sHostInstrumentation;
    private static Instrumentation sBundleInstrumentation;

    private static ConcurrentHashMap<String, PluginDexLoader.LoadedApk> sLoadedApks; //key:packageName

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
    }

    @Override
    public void postSetUp() {
        super.postSetUp();
        for (PluginRecord pluginRecord : PluginManager.mPluginRecords) {
            mergeDexAndResource(pluginRecord);
        }
    }

    // todo 还需要支持apk,so,jar等类型
    @Override
    public void loadPlugin(PluginRecord pluginRecord) {
        if (sLoadedApks == null) sLoadedApks = new ConcurrentHashMap<>();
        PluginDexLoader.LoadedApk apk = sLoadedApks.get(pluginRecord.getPackageInfo().packageName);
        if (apk == null) {
            apk = new PluginDexLoader.LoadedApk();
            apk.id = pluginRecord.getPluginInfo().id;
            apk.version = pluginRecord.getPluginInfo().version;
            apk.packageName = pluginRecord.getPluginInfo().packageName;
            apk.path = PluginUtil.getPluginPath(pluginRecord.getPluginInfo().apkFileName);
            sLoadedApks.put(pluginRecord.getPackageInfo().packageName, apk);
        }
    }

    private void mergeDexAndResource(PluginRecord pluginRecord) {
        if (sLoadedApks == null) {
            return;
        }

        PluginDexLoader.LoadedApk apk = sLoadedApks.get(pluginRecord.getPackageInfo().packageName);
        if (apk != null) {
            try {
                String path = apk.path;
                String apkName = pluginRecord.getPluginInfo().apkFileName;
                String dexName = apkName.replace(".apk", ".dex");
                if (path.endsWith(".apk")) {
                    Log.i(TAG, "loadPlugin: plugin -> " + apkName);
                    Utils.extractAssets(PluginManager.mBaseContext, path);
                    PluginDexLoader.mergeDexs(apkName, dexName);
                    PluginDexLoader.updateResource(path);
                }
            } catch (Exception e) {
                Log.e(TAG, "error , loadPlugin error ", e);
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
