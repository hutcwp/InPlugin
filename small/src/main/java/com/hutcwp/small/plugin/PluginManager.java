package com.hutcwp.small.plugin;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import com.hutcwp.small.luancher.ApkPluginLauncher;
import com.hutcwp.small.luancher.PluginLauncher;
import com.hutcwp.small.util.JsonUtil;
import com.hutcwp.small.util.PluginController;
import com.hutcwp.small.util.RefInvoke;

import java.util.ArrayList;
import java.util.List;

public enum PluginManager {

    INSTANCE;

    private static final String TAG = "PluginManager";

    public static List<PluginRecord> mPluginRecords = new ArrayList<>();
    private List<PluginLauncher> mPluginLaunchers = null;
    public static volatile Resources mNowResources;
    public static volatile Context mBaseContext;

    //ContextImpl中的LoadedAPK对象mPackageInfo
    public static Object mPackageInfo = null;

    public static void init(Application application) {
        mPackageInfo = RefInvoke.getFieldObject(application.getBaseContext(), "mPackageInfo");
        mBaseContext = application.getBaseContext();
        mNowResources = mBaseContext.getResources();
    }

    public void preSetUp(Application application) {
        PluginManager.INSTANCE.registerLauncher(new ApkPluginLauncher());
        PluginManager.INSTANCE.preSetUpLaunchers(application);
    }

    public boolean setup(Context context) {
        setupLaunchers(context);
        // 通过plugin.json解析出，需要加载的插件信息
        List<PluginInfo> pluginInfos = JsonUtil.getPluginConfig(JsonUtil.pluginJsonStr);
        if (pluginInfos == null) {
            Log.e(TAG, "pluginInfos is null, return false!!!");
            return false;
        }

        for (PluginInfo info : pluginInfos) {
            PluginRecord pluginRecord = PluginRecord.generatePluginRecord(mBaseContext, info);
            mPluginRecords.add(pluginRecord);
            loadSetupPlugins();
        }
        postSetUpLauncher();
        return true;
    }

    public void loadSetupPlugins() {
        for (PluginRecord pluginRecord : mPluginRecords) {
            if (pluginRecord.getPackageInfo() != null) {
                PluginController.addLoadActivity(pluginRecord.getPackageInfo().activities);
            }
        }
    }

    /**
     * 注册加载器
     *
     * @param launcher
     */
    private void registerLauncher(PluginLauncher launcher) {
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
    private void preSetUpLaunchers(Application context) {
        if (mPluginLaunchers == null) {
            return;
        }

        for (PluginLauncher launcher : mPluginLaunchers) {
            launcher.preSetUp(context);
        }
    }

    /**
     * 启动初始化加载器
     *
     * @param context applicationContext
     */
    private void setupLaunchers(Context context) {
        if (mPluginLaunchers == null) {
            return;
        }

        for (PluginLauncher launcher : mPluginLaunchers) {
            launcher.setUp(context);
        }
    }

    private void postSetUpLauncher() {
        if (mPluginLaunchers == null) {
            return;
        }

        for (PluginLauncher launcher : mPluginLaunchers) {
            launcher.postSetUp();
        }
    }
}
