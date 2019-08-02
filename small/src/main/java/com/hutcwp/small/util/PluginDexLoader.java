package com.hutcwp.small.util;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;
import com.hutcwp.small.hook.BaseDexClassLoaderHookHelper;
import com.hutcwp.small.plugin.PluginManager;
import dalvik.system.DexFile;

import java.io.File;
import java.lang.reflect.Method;

/**
 * Created by hutcwp on 2019-08-02 15:10
 * email: caiwenpeng@yy.com
 * YY: 909076244
 **/
public class PluginDexLoader {

    private static final String TAG = "PluginDexLoader";

    public static class LoadedApk {
        public String id;
        public String version;
        public String packageName;
        public File packagePath;
        public String applicationName;
        public String path;
        public DexFile dexFile;
        public File libraryPath;
        public boolean nonResources; /** no resources.arsc */
    }

    public static void updateResource(String pluginPath) {
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

    public static void mergeDexs(String apkName, String dexName) {
        File dexFile = PluginManager.mBaseContext.getFileStreamPath(apkName);
        File optDexFile = PluginManager.mBaseContext.getFileStreamPath(dexName);
        try {
            BaseDexClassLoaderHookHelper.patchClassLoader(
                    PluginManager.mBaseContext.getClassLoader(), dexFile, optDexFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
