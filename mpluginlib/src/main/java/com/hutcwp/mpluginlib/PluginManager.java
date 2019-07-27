package com.hutcwp.mpluginlib;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.Window;
import com.hutcwp.mpluginlib.plugin.PluginParser;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class PluginManager {
    public final static List<PluginItem> plugins = new ArrayList<PluginItem>();

    //正在使用的Resources
    public static volatile Resources mNowResources;

    //原始的application中的BaseContext，不能是其他的，否则会内存泄漏
    public static volatile Context mBaseContext;

    //ContextImpl中的LoadedAPK对象mPackageInfo
    private static Object mPackageInfo = null;

    public static void init(Application application) {
        //初始化一些成员变量和加载已安装的插件
        mPackageInfo = RefInvoke.getFieldObject(application.getBaseContext(), "mPackageInfo");
        mBaseContext = application.getBaseContext();
        mNowResources = mBaseContext.getResources();

        try {
//            AssetManager assetManager = application.getAssets();
//            String[] paths = assetManager.list("");

            // 从存储卡/mlugins/路径下读取
            String[] paths = new String[]{};
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                String dirPath = Environment.getExternalStorageDirectory().getAbsoluteFile() + File.separator
                        + "mplugins/";
                File dir = new File(dirPath);
                Log.i("test", "dirPath = " + dirPath);
                if (dir.exists() && dir.isDirectory()) {
                    paths = dir.list();
                    for (int i = 0; i < paths.length; i++) {
                        Log.i("test", "dir list path = " + dir.list()[i]);
                    }
                }


                ArrayList<String> pluginPaths = new ArrayList<String>();
                for (String path : paths) {
                    if (path.endsWith(".apk")) {
                        Log.i("test", "parse apk plugin " + path);
                        String apkName = path;
                        String dexName = apkName.replace(".apk", ".dex");

                        Utils.extractAssets(mBaseContext, apkName);
                        mergeDexs(apkName, dexName);

                        PluginItem item = generatePluginItem(apkName);
                        plugins.add(item);

                        pluginPaths.add(item.pluginPath);
                    }
                }

                reloadInstalledPluginResources(pluginPaths);
            }

            hookInstrumentation();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void hookInstrumentation() {
        try {
            // 先获取到当前的ActivityThread对象
            Object currentActivityThread =
                    RefInvoke.getStaticFieldObject("android.app.ActivityThread", "sCurrentActivityThread");
            Class sActivitythread_class = Class.forName("android.app.ActivityThread");
            Instrumentation host = (Instrumentation) RefInvoke.getFieldObject(currentActivityThread,
                    "mInstrumentation");
            InstrumentationWrapper mInstrumentationWrapper = new InstrumentationWrapper(host);
            RefInvoke.setFieldObject(currentActivityThread.getClass(), currentActivityThread,
                    "mInstrumentation", mInstrumentationWrapper);
        } catch (ClassNotFoundException e) {
            Log.e("test", "hookInstrumentation error.");
            e.printStackTrace();
        }
    }

    private static PluginItem generatePluginItem(String apkName) {
        File file = mBaseContext.getFileStreamPath(apkName);
        PluginItem item = new PluginItem();
        item.pluginPath = file.getAbsolutePath();
        item.packageInfo = DLUtils.getPackageInfo(mBaseContext, item.pluginPath);
        item.pluginParser = PluginParser.parsePackage(file);

        return item;
    }

    static void mergeDexs(String apkName, String dexName) {

        File dexFile = mBaseContext.getFileStreamPath(apkName);
        File optDexFile = mBaseContext.getFileStreamPath(dexName);

        try {
            BaseDexClassLoaderHookHelper.patchClassLoader(mBaseContext.getClassLoader(), dexFile, optDexFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void reloadInstalledPluginResources(ArrayList<String> pluginPaths) {
        try {
            AssetManager assetManager = AssetManager.class.newInstance();
            Method addAssetPath = AssetManager.class.getMethod("addAssetPath", String.class);

            addAssetPath.invoke(assetManager, mBaseContext.getPackageResourcePath());

            for (String pluginPath : pluginPaths) {
                addAssetPath.invoke(assetManager, pluginPath);
                Log.i("test", "addAssetPath path = " + pluginPath);
            }


            Resources newResources = new Resources(assetManager,
                    mBaseContext.getResources().getDisplayMetrics(),
                    mBaseContext.getResources().getConfiguration());


            RefInvoke.setFieldObject(mBaseContext, "mResources", newResources);
            //这是最主要的需要替换的，如果不支持插件运行时更新，只留这一个就可以了
            RefInvoke.setFieldObject(mPackageInfo, "mResources", newResources);

            mNowResources = newResources;
            //需要清理mTheme对象，否则通过inflate方式加载资源会报错
            //如果是activity动态加载插件，则需要把activity的mTheme对象也设置为null
            RefInvoke.setFieldObject(mBaseContext, "mTheme", null);
        } catch (Throwable e) {
            Log.e("test", "reloadInstalledPluginResources error.", e);
            e.printStackTrace();
        }
    }


    static class InstrumentationWrapper extends Instrumentation {
        Instrumentation rawInstrumentation;

        public InstrumentationWrapper(Instrumentation rawInstrumentatio) {
            this.rawInstrumentation = rawInstrumentatio;
        }

        @Override
        /** Prepare resources for REAL */
        public void callActivityOnCreate(Activity activity, android.os.Bundle icicle) {
            Log.i("test", "callActivityOnCreate");
            ActivityInfo[] activities = PluginManager.plugins.get(0).pluginParser.getPackageInfo().activities;
            for (ActivityInfo ai : activities) {
                if (ai.name.equals(activity.getClass().getName())) {
                    Log.e("test", "find out activityInfo , name is " + ai.name);
                    applyActivityInfo(activity, ai);
                    break;
                }
            }

            rawInstrumentation.callActivityOnCreate(activity, icicle);
        }

        private void applyActivityInfo(Activity activity, ActivityInfo ai) {
            Log.i("test", "applyActivityInfo");
            if (Build.VERSION.SDK_INT >= 28) {
                ReflectAccelerator.resetResourcesAndTheme(activity, ai.getThemeResource());
            }

            Window window = activity.getWindow();
            window.setSoftInputMode(ai.softInputMode);
            activity.setRequestedOrientation(ai.screenOrientation);
        }
    }
}
