package com.hutcwp.cow.plugin;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;
import com.hutcwp.cow.luancher.PluginLauncher;
import com.hutcwp.cow.util.DLUtils;
import com.hutcwp.cow.hook.BaseDexClassLoaderHookHelper;
import com.hutcwp.cow.util.RefInvoke;
import com.hutcwp.cow.util.Utils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public enum PluginManager {

    INSTANCE;

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
        return true;
    }

    public void loadSetupPlugins() {

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


        } catch (Exception e) {
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


}
