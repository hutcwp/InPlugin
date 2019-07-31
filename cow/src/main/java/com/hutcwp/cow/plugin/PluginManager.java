package com.hutcwp.cow.plugin;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;
import com.hutcwp.cow.hook.BaseDexClassLoaderHookHelper;
import com.hutcwp.cow.luancher.PluginLauncher;
import com.hutcwp.cow.util.DLUtils;
import com.hutcwp.cow.util.JsonUtil;
import com.hutcwp.cow.util.PluginController;
import com.hutcwp.cow.util.RefInvoke;
import com.hutcwp.cow.util.Utils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public enum PluginManager {

    INSTANCE;
    private static final String TAG = "PluginManager";


    private List<PluginLauncher> mPluginLaunchers = null;

    private Context mApplicationContext;

    /**
     * 注册加载器
     *
     * @param launcher
     */
    public void registerLauncher(PluginLauncher launcher) {
        if (mPluginLaunchers == null) {
            mPluginLaunchers = new ArrayList<>();
        }
        mPluginLaunchers.add(launcher);
    }

    /**
     * 初始化加载器
     *
     * @param context applicationContext
     */
    public void initLaunchers(Application context) {
        mApplicationContext = context;
        if (mPluginLaunchers == null) {
            return;
        }

        for (PluginLauncher launcher : mPluginLaunchers) {
            launcher.preSetUp(context);
        }
    }

    public boolean setup(Context context) {
        setupLaunchers(context);
        List<PluginInfo> pluginInfos = JsonUtil.getPluginConfig(JsonUtil.pluginJsonStr);
        List<PluginRecord> pluginRecords = new ArrayList<>();
        for (PluginInfo info : pluginInfos) {
            PluginRecord pluginRecord = new PluginRecord();
            pluginRecord.setPluginInfo(info);
            pluginRecords.add(pluginRecord);
        }

        //  逐个loadPlugin
        for (PluginRecord pluginRecord : pluginRecords) {
            loadPlugin(pluginRecord);
        }
        return true;
    }

    public void loadSetupPlugins() {
        for (PluginRecord pluginRecord : plugins) {
            PluginController.addLoadActivity(pluginRecord.packageInfo.activities);
        }
    }

    /**
     * 启动初始化加载器
     *
     * @param context applicationContext
     */
    public void setupLaunchers(Context context) {
        if (mPluginLaunchers == null) {
            return;
        }

        for (PluginLauncher launcher : mPluginLaunchers) {
            launcher.setUp(context);
        }
    }


    public final static List<PluginRecord> plugins = new ArrayList<PluginRecord>();

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
    }

    private void loadPlugin(PluginRecord pluginRecord) {
        Log.i(TAG, "loadPlugin: pluginRecord = " + pluginRecord.toString());
        try {
//            AssetManager assetManager = application.getAssets();
//            String[] paths = assetManager.list("");

            // 从存储卡/mlugins/路径下读取
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                String dirPath = Environment.getExternalStorageDirectory().getAbsoluteFile() + File.separator
                        + "mplugins";

                String path = getPluginPath(dirPath, pluginRecord.getPluginInfo().apkFileName);
                String apkName = pluginRecord.getPluginInfo().apkFileName;
                String dexName = apkName.replace(".apk", ".dex");
                if (path.endsWith(".apk")) {
                    Log.i(TAG, "parse apk plugin " + path);

                    Utils.extractAssets(mBaseContext, path);
                    mergeDexs(apkName, dexName);

                    PluginRecord item = generatePluginItem(apkName);
                    plugins.add(item);
                    updateResource(path);
                }
            }
        } catch (Exception e) {
            Log.e("test", "error , e ", e);
        }
    }

    private void updateResource(String pluginPath) {
        Log.i(TAG, "updateResource");
        try {
            if (mNowResources == null) {
                Log.i(TAG, "init resource");
                AssetManager assetManager = AssetManager.class.newInstance();
                Method addAssetPath = AssetManager.class.getMethod("addAssetPath", String.class);
                addAssetPath.invoke(assetManager, mBaseContext.getPackageResourcePath());
                addAssetPath.invoke(assetManager, pluginPath);
                Log.i(TAG, "addAssetPath path = " + pluginPath);

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
            } else {
                AssetManager assetManager = mNowResources.getAssets();
                Method addAssetPath = AssetManager.class.getMethod("addAssetPath", String.class);
                addAssetPath.invoke(assetManager, pluginPath);
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
            }
        } catch (Throwable e) {
            Log.e(TAG, "reloadInstalledPluginResources error.", e);
            e.printStackTrace();
        }
    }

    private String getPluginPath(String pathDir, String apkFileName) {
        return pathDir + File.separator + apkFileName;
    }


    private static PluginRecord generatePluginItem(String apkName) {
        File file = mBaseContext.getFileStreamPath(apkName);
        PluginRecord item = new PluginRecord();
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


}
