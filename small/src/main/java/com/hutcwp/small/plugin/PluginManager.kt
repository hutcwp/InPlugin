package com.hutcwp.small.plugin

import android.app.Application
import android.content.Context
import android.content.pm.ActivityInfo
import android.util.Log
import com.hutcwp.small.Small
import com.hutcwp.small.luancher.ApkPluginLauncher
import com.hutcwp.small.luancher.PluginLauncher
import com.hutcwp.small.util.JsonUtil
import com.hutcwp.small.util.PluginUtil
import com.hutcwp.small.util.Utils

import java.util.ArrayList
import java.util.concurrent.ConcurrentHashMap

enum class PluginManager {

    INSTANCE;

    private var mPluginLaunchers: MutableList<PluginLauncher>? = null


    fun preSetUp(application: Application) {
        registerLauncher(ApkPluginLauncher())
        preSetUpLaunchers(application)
    }

    fun setup(context: Context): Boolean {
        setupLaunchers(context)
        return true
    }

    fun activePlugin() {
        for (plugin in mPlugins.values) {
            plugin.pluginRecord.activePlugin()
            plugin.pluginRecord.execPlugin()
        }
    }

    /**
     * 加载内置插件
     */
    fun loadSetupPlugins() {
        // 通过plugin.json解析出，需要加载的插件信息
        val pluginInfos = JsonUtil.getPluginConfig(JsonUtil.pluginJsonStr)
        if (pluginInfos == null) {
            Log.e(TAG, "parse pluginInfos is null, return false!!!")
            return
        }

        for (pluginInfo in pluginInfos) {
            // 复制apk
            Utils.extractAssets(Small.mBaseContext, PluginUtil.getPluginPath(pluginInfo.apkFileName))
            val pluginRecord = PluginRecord.generatePluginRecord(
                Small.mBaseContext, pluginInfo, mPluginLaunchers
            )
            val plugin = Plugin(pluginInfo, pluginRecord)
            mPlugins[plugin.pluginInfo.id] = plugin
        }
        for (plugin in mPlugins.values) {
            // 内置插件
            if (plugin.pluginInfo.loadMode == 0) {
                plugin.pluginRecord.launch()
            }
        }
        postSetUpLauncher()
    }

    fun loadSinglePlugin(pluginId: String) {
        mPlugins[pluginId]?.let {
            loadPlugins(it)
        }
    }

    /**
     * 加载插件
     */
    private fun loadPlugins(plugin: Plugin) {
        plugin.pluginRecord.launch()
        postSetUpLauncher()
    }

    /**
     * 注册加载器
     *
     * @param launcher
     */
    private fun registerLauncher(launcher: PluginLauncher) {
        if (mPluginLaunchers == null) {
            mPluginLaunchers = ArrayList()
        }
        mPluginLaunchers!!.add(launcher)
    }

    /**
     * 初始化加载器
     *
     * @param context applicationContext
     */
    private fun preSetUpLaunchers(context: Application) {
        mPluginLaunchers?.forEach { launcher ->
            launcher.preSetUp(context)
        }
    }

    /**
     * 启动初始化加载器
     *
     * @param context applicationContext
     */
    private fun setupLaunchers(context: Context) {
        mPluginLaunchers?.forEach { launcher ->
            launcher.setUp(context)
        }
    }

    /**
     * 启动完成
     */
    private fun postSetUpLauncher() {
        mPluginLaunchers?.forEach { launcher ->
            launcher.postSetUp()
        }
    }

    companion object {
        private const val TAG = "PluginManager"
        var mPlugins = ConcurrentHashMap<String, Plugin>() //用插件id做key，唯一
    }
}
