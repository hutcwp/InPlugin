package com.hutcwp.pluginb

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import com.hutcwp.mpluginlib.ZeusBaseActivity

class PluginBActivity : ZeusBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("PluginBActivity", "pluginB PluginBActivity onCreate")
        setContentView(R.layout.activity_plugin_b)
        val pm = packageManager //获取PackageManager实例
        val pmg = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES) //获取已经安装的包的信息 Installe 安装

        try {
            val ain = packageManager
                .getPackageInfo(pmg[0].packageName, PackageManager.GET_ACTIVITIES)
                .activities //直接通过packageManager来获取activityinfo对象
            if (ain == null) {
                Log.d("PluginBActivity", "get activityInfo fail ")
            } else {
                Log.d("PluginBActivity", "activityInfo:  " + ain.size + " " + ain.hashCode())
                Log.d("PluginBActivity", "ain[0].screenOrientation  = ${ain[0].screenOrientation}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clickView(v: View) {
        startActivity(Intent(this, PluginBMainActivity::class.java))
    }

}
