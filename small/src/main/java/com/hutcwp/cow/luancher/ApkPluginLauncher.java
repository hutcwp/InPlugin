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

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;
import com.hutcwp.cow.hook.BaseDexClassLoaderHookHelper;
import com.hutcwp.cow.internal.InstrumentationWrapper;
import com.hutcwp.cow.plugin.PluginManager;
import com.hutcwp.cow.plugin.PluginRecord;
import com.hutcwp.cow.util.PluginUtil;
import com.hutcwp.cow.util.RefInvoke;
import com.hutcwp.cow.util.ReflectAccelerator;
import com.hutcwp.cow.util.Utils;

import java.io.File;
import java.lang.reflect.Method;


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
        for (PluginRecord pluginRecord : PluginManager.pluginRecords) {
            loadPlugin(pluginRecord);
        }
    }

    // todo 还需要支持apk,so,jar等类型
    private void loadPlugin(PluginRecord pluginRecord) {
        try {
            String path = PluginUtil.getPluginPath(pluginRecord.getPluginInfo().apkFileName);
            String apkName = pluginRecord.getPluginInfo().apkFileName;
            String dexName = apkName.replace(".apk", ".dex");
            if (path.endsWith(".apk")) {
                Log.i(TAG, "loadPlugin: plugin -> " + apkName);
                Utils.extractAssets(PluginManager.mBaseContext, path);
                mergeDexs(apkName, dexName);
                updateResource(path);
            }
        } catch (Exception e) {
            Log.e(TAG, "error , loadPlugin error ", e);
        }
    }

    private void updateResource(String pluginPath) {
        Log.i(TAG, "updateResource");
        try {
            if (PluginManager.mNowResources == null) {
                Log.i(TAG, "init resource");
                AssetManager assetManager = AssetManager.class.newInstance();
                Method addAssetPath = AssetManager.class.getMethod("addAssetPath", String.class);
                addAssetPath.invoke(assetManager, PluginManager.mBaseContext.getPackageResourcePath());
                addAssetPath.invoke(assetManager, pluginPath);
                Log.i(TAG, "addAssetPath path = " + pluginPath);

                Resources newResources = new Resources(assetManager,
                        PluginManager.mBaseContext.getResources().getDisplayMetrics(),
                        PluginManager.mBaseContext.getResources().getConfiguration());

                RefInvoke.setFieldObject(PluginManager.mBaseContext, "mResources", newResources);
                //这是最主要的需要替换的，如果不支持插件运行时更新，只留这一个就可以了
                RefInvoke.setFieldObject(PluginManager.mPackageInfo, "mResources", newResources);

                PluginManager.mNowResources = newResources;
                //需要清理mTheme对象，否则通过inflate方式加载资源会报错
                //如果是activity动态加载插件，则需要把activity的mTheme对象也设置为null
                RefInvoke.setFieldObject(PluginManager.mBaseContext, "mTheme", null);
            } else {
                AssetManager assetManager = PluginManager.mNowResources.getAssets();
                Method addAssetPath = AssetManager.class.getMethod("addAssetPath", String.class);
                addAssetPath.invoke(assetManager, pluginPath);
                Resources newResources = new Resources(assetManager,
                        PluginManager.mBaseContext.getResources().getDisplayMetrics(),
                        PluginManager.mBaseContext.getResources().getConfiguration());

                RefInvoke.setFieldObject(PluginManager.mBaseContext, "mResources", newResources);
                //这是最主要的需要替换的，如果不支持插件运行时更新，只留这一个就可以了
                RefInvoke.setFieldObject(PluginManager.mPackageInfo, "mResources", newResources);

                PluginManager.mNowResources = newResources;
                //需要清理mTheme对象，否则通过inflate方式加载资源会报错
                //如果是activity动态加载插件，则需要把activity的mTheme对象也设置为null
                RefInvoke.setFieldObject(PluginManager.mBaseContext, "mTheme", null);
            }
        } catch (Throwable e) {
            Log.e(TAG, "updateResource error.", e);
        }
    }

    private void mergeDexs(String apkName, String dexName) {
        File dexFile = PluginManager.mBaseContext.getFileStreamPath(apkName);
        File optDexFile = PluginManager.mBaseContext.getFileStreamPath(dexName);
        try {
            BaseDexClassLoaderHookHelper.patchClassLoader(PluginManager.mBaseContext.getClassLoader(), dexFile, optDexFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
