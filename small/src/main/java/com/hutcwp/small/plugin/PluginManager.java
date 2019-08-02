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
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public enum PluginManager {

    INSTANCE;

    private static final String TAG = "PluginManager";

    public static List<PluginRecord> mPluginRecords = new ArrayList<>();
    private List<PluginLauncher> mPluginLaunchers = null;
    public static volatile Resources mNowResources;
    public static volatile Context mBaseContext;

    public static Object mPackageInfo = null;

    private final Queue<AsyncLoadRequest> mAsyncLoadRequestQueue = new LinkedList<>();

    private boolean hasInit = false;

    private class AsyncLoadRequest {
        public final PluginRecord plugin;

        public AsyncLoadRequest(PluginRecord plugin) {
            this.plugin = plugin;
        }
    }

    public void init(Application application) {
        mPackageInfo = RefInvoke.getFieldObject(application.getBaseContext(), "mPackageInfo");
        mBaseContext = application.getBaseContext();
        mNowResources = mBaseContext.getResources();
        hasInit = true;
    }

    public void preSetUp(Application application) {
        if (!hasInit) {
            init(application);
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

        // PluginInfo 转 Plugin
        List<Plugin> plugins = new ArrayList<>();
        for (PluginInfo pluginInfo : pluginInfos) {
            Plugin plugin = new Plugin(pluginInfo);
            plugins.add(plugin);
        }

        for (Plugin plugin : plugins) {
            PluginRecord pluginRecord = PluginRecord.generatePluginRecord(mBaseContext, plugin.getPluginInfo());
            pluginRecord.applyLaunchers(mPluginLaunchers);
            mPluginRecords.add(pluginRecord);
            pluginRecord.launch();
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
            loadPlugin(pluginRecord);
        }
    }

    private void loadPlugin(PluginRecord pluginRecord) {
        Log.i(TAG, "loadPlugin");
        AsyncLoadRequest request = null;
        request = new AsyncLoadRequest(pluginRecord);
        synchronized (mAsyncLoadRequestQueue) {
            mAsyncLoadRequestQueue.add(request);
        }
        loadPluginsCore();
    }

    private void loadPluginsCore() {
        synchronized (mAsyncLoadRequestQueue) {
            while (!mAsyncLoadRequestQueue.isEmpty()) {
                AsyncLoadRequest asyncLoadRequest = mAsyncLoadRequestQueue.remove();
                Log.i(TAG, "mAsyncLoadRequestQueue.size = " + mAsyncLoadRequestQueue.size());
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
