package com.hutcwp.small.plugin

import android.app.Application
import android.content.Context
import android.util.Log
import com.hutcwp.small.Small
import com.hutcwp.small.luancher.ApkPluginLauncher
import com.hutcwp.small.luancher.PluginLauncher
import com.hutcwp.small.util.JsonUtil
import com.hutcwp.small.util.PluginUtil
import com.hutcwp.small.util.Utils
import java.util.*
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

    /**
     * 启动单个插件
     */
    fun activeSinglePlugin(pluginId: String, listener: Small.OnActiveListener): Boolean {
        if (mPlugins[pluginId] == null) {
            Log.i(TAG, "pluginId $pluginId not find in mPlugins")
            return false
        }
        return activePlugins(listOf(mPlugins[pluginId]!!), listener)
    }

    /**
     * 批量启动插件
     */
    private fun activePlugins(pluginList: List<Plugin>, listener: Small.OnActiveListener): Boolean {
        pluginList.forEach {
            if (mPlugins[it.pluginInfo.id] == null) {
                Log.i(TAG, "plugin not find in mPlugin List, Please check you plugin id define in `plugin.json`")
                listener.onActive(Small.ActivePluginResult.PluginActiveFail)
            } else {
                if (!it.isEnable) {
                    Log.e(TAG, "plugin id[${it.pluginInfo.id}] is not enable!")
                    listener.onActive(Small.ActivePluginResult.PluginActiveFail)
                }
                if (!it.pluginRecord.activePlugin()) {
                    listener.onActive(Small.ActivePluginResult.PluginActiveFail)
                    Log.e(TAG, "plugin id[${it.pluginInfo.id}] activePlugin failed!")
                }
            }
        }
        listener.onActive(Small.ActivePluginResult.PluginActiveSuccess)
        return true
    }

    /**
     * 加载内置插件
     */
    fun loadSetupPlugins() {
        parsePluginsFromJson()
        mPlugins.values.forEach { plugin ->
            // 内置插件
            if (plugin.pluginInfo.loadMode == 0) {
                plugin.pluginRecord.launch()
            }
        }
        postSetUpLauncher()
    }

    private fun parsePluginsFromJson() {
        // 通过plugin.json解析出，需要加载的插件信息
        val pluginInfos = JsonUtil.getPluginConfig(JsonUtil.pluginJsonStr)
        if (pluginInfos == null) {
            Log.e(TAG, "parse pluginInfos is null, return false!!!")
            return
        }

        for (pluginInfo in pluginInfos) {
            // 复制apk ，todo 复制不应该放在这里
            Utils.extractAssets(Small.getContext(), PluginUtil.getPluginPath(pluginInfo.apkFileName))
            val pluginRecord = PluginRecord.generatePluginRecord(
                Small.getContext(), pluginInfo, mPluginLaunchers
            )
            val plugin = Plugin(pluginInfo, pluginRecord)
            mPlugins[plugin.pluginInfo.id] = plugin
        }
    }

    /**
     * 加载单个插件
     */
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

    /**
     * 插件是否可用
     */
    fun pluginEnable(id: String): Boolean {
        val plugin = mPlugins[id]
        return plugin?.isEnable ?: false
    }

    companion object {
        private const val TAG = "PluginManager"
        // 代表所有的插件
        var mPlugins = ConcurrentHashMap<String, Plugin>() //用插件id做key，唯一
    }
}
