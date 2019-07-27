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
    }

    fun clickView(v: View) {
        startActivity(Intent(this, PluginBMainActivity::class.java))
    }

}
