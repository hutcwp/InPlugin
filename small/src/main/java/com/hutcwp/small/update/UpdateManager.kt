package com.hutcwp.small.update

import android.util.Log
import com.hutcwp.small.Small
import com.hutcwp.small.plugin.PluginInfo
import com.hutcwp.small.util.JsonUtil
import com.hutcwp.small.util.PluginUtil
import com.hutcwp.small.util.Utils
import java.io.File

/**
 *
 * Created by hutcwp on 2019-08-10 23:00
 * email: caiwenpeng@yy.com
 * YY: 909076244
 *
 **/
enum class UpdateManager {

    INSTANCE;


    fun parsePluginsFromJson(): List<PluginInfo>? {
        // 通过plugin.json解析出，需要加载的插件信息
        val pluginInfos = JsonUtil.getPluginConfig(JsonUtil.pluginJsonStr)
        pluginInfos.forEach { pluginInfo ->
            val pluginFile = File(PluginUtil.getPluginPath(pluginInfo.apkFileName))
            if (pluginFile.exists()) {
                Utils.extractAssets(Small.getContext(), PluginUtil.getPluginPath(pluginInfo.apkFileName))
            } else {
                Log.i(TAG, "plugin[${pluginFile.name}] not found.")
                pluginInfos.remove(pluginInfo)
            }

        }
        return pluginInfos
    }

    companion object {
        const val TAG = "UpdateManager"
    }

}