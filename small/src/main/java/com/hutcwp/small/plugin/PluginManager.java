package com.hutcwp.small.plugin;

import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.Log;
import com.hutcwp.small.Small;
import com.hutcwp.small.luancher.ApkPluginLauncher;
import com.hutcwp.small.luancher.PluginLauncher;
import com.hutcwp.small.util.JsonUtil;
import com.hutcwp.small.util.PluginUtil;
import com.hutcwp.small.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public enum PluginManager {

    INSTANCE;

    private static final String TAG = "PluginManager";

    private boolean hasInit = false;
    private List<PluginLauncher> mPluginLaunchers = null;
    public static ConcurrentHashMap<String, Plugin> mPlugins = new ConcurrentHashMap<>(); //用插件id做key，唯一


    public void preSetUp(Application application) {
        if (!hasInit) {
            hasInit = true;
        }

        registerLauncher(new ApkPluginLauncher());
        preSetUpLaunchers(application);
    }

    public boolean setup(Context context) {
        if (!hasInit) {
            Log.e(TAG, "have you invoke init method?");
            return false;
        }

        setupLaunchers(context);
        // 通过plugin.json解析出，需要加载的插件信息
        List<PluginInfo> pluginInfos = JsonUtil.getPluginConfig(JsonUtil.pluginJsonStr);
        if (pluginInfos == null) {
            Log.e(TAG, "parse pluginInfos is null, return false!!!");
            return false;
        }

        for (PluginInfo pluginInfo : pluginInfos) {
            // 复制apk
            Utils.extractAssets(Small.mBaseContext, PluginUtil.getPluginPath(pluginInfo.apkFileName));
            PluginRecord pluginRecord = PluginRecord.generatePluginRecord(
                    Small.mBaseContext, pluginInfo, mPluginLaunchers);
            Plugin plugin = new Plugin(pluginInfo, pluginRecord);
            mPlugins.put(plugin.getPluginInfo().id, plugin);
        }
        loadSetupPlugins();
        postSetUpLauncher();
        return true;
    }

    /**
     * 加载内置插件
     */
    public void loadSetupPlugins() {
        for (Plugin plugin : mPlugins.values()) {
            plugin.getPluginRecord().launch();
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

    public ActivityInfo getActivityInfoByQuery(String className) {
        for (Plugin plugin : mPlugins.values()) {
            for (ActivityInfo activityInfo : plugin.getPluginRecord().getPluginParser().getPackageInfo().activities) {
                if (activityInfo.name.equals(className)) {
                    return activityInfo;
                }
            }
        }

        return null;
    }
}
