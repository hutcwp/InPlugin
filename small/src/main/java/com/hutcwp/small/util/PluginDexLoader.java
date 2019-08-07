package com.hutcwp.small.util;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;
import com.hutcwp.small.Small;
import com.hutcwp.small.hook.BaseDexClassLoaderHookHelper;

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
        public File dexFile;
        public File apkFile;
        public File libraryPath;
        public boolean nonResources; /** no resources.arsc */
    }

    public static void updateResource(String pluginPath) {
        Log.i(TAG, "updateResource");
        try {
            if (Small.getResources() == null) {
                Log.i(TAG, "init resource");
                AssetManager assetManager = AssetManager.class.newInstance();
                Method addAssetPath = AssetManager.class.getMethod("addAssetPath", String.class);
                addAssetPath.invoke(assetManager, Small.getContext().getPackageResourcePath());
                addAssetPath.invoke(assetManager, pluginPath);
                Log.i(TAG, "addAssetPath path = " + pluginPath);

                Resources newResources = new Resources(assetManager,
                        Small.getContext().getResources().getDisplayMetrics(),
                        Small.getContext().getResources().getConfiguration());

                RefInvoke.setFieldObject(Small.getContext(), "mResources", newResources);
                //这是最主要的需要替换的，如果不支持插件运行时更新，只留这一个就可以了
                RefInvoke.setFieldObject(ReflectAccelerator.mPackageInfo, "mResources", newResources);

                Small.setNowResources(newResources);
                //需要清理mTheme对象，否则通过inflate方式加载资源会报错
                //如果是activity动态加载插件，则需要把activity的mTheme对象也设置为null
                RefInvoke.setFieldObject(Small.getContext(), "mTheme", null);
            } else {
                AssetManager assetManager = Small.getResources().getAssets();
                Method addAssetPath = AssetManager.class.getMethod("addAssetPath", String.class);
                addAssetPath.invoke(assetManager, pluginPath);
                Resources newResources = new Resources(assetManager,
                        Small.getContext().getResources().getDisplayMetrics(),
                        Small.getContext().getResources().getConfiguration());

                // todo 这里有哥异常，后面再看
                RefInvoke.setFieldObject(Small.getContext(), "mResources", newResources);
                //这是最主要的需要替换的，如果不支持插件运行时更新，只留这一个就可以了
                RefInvoke.setFieldObject(ReflectAccelerator.mPackageInfo, "mResources", newResources);

                Small.setNowResources(newResources);
                //需要清理mTheme对象，否则通过inflate方式加载资源会报错
                //如果是activity动态加载插件，则需要把activity的mTheme对象也设置为null
                RefInvoke.setFieldObject(Small.getContext(), "mTheme", null);
            }
        } catch (Throwable e) {
            Log.e(TAG, "updateResource error.", e);
        }
    }

    public static void mergeDexs(String apkName, String dexName) {
        File dexFile = Small.getContext().getFileStreamPath(apkName);
        File optDexFile = Small.getContext().getFileStreamPath(dexName);
        try {
            BaseDexClassLoaderHookHelper.patchClassLoader(
                    Small.getContext().getClassLoader(), dexFile, optDexFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void mergeDexs(File dexFile, File optDexFile) {
        try {
            BaseDexClassLoaderHookHelper.patchClassLoader(
                    Small.getContext().getClassLoader(), dexFile, optDexFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
