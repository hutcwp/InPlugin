package com.hutcwp.inplugin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.hutcwp.small.Small
import com.hutcwp.small.plugin.PluginManager
import kotlinx.android.synthetic.main.activity_plugin_info.*

class PluginInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plugin_info)

        val pluginInfoText = StringBuffer()
        Small.getPlugins().forEach { plugin ->
            pluginInfoText.append("plugin Id: ${plugin.pluginInfo?.id}\n")
            pluginInfoText.append("plugin package: ${plugin.pluginInfo?.packageName}\n")
            pluginInfoText.append("plugin enable: ${plugin.isEnable}\n")
            pluginInfoText.append("plugin file: ${plugin.getmApkFile().absoluteFile}\n\n")
        }
        tvPluginInfo?.text = pluginInfoText
    }
}
