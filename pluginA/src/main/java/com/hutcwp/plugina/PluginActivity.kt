package com.hutcwp.plugina

import android.app.Activity
import android.content.res.AssetManager
import android.content.res.Resources
import android.os.Bundle
import android.widget.Button


class PluginActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val btn = Button(this)
        btn.text = "button create"
        setContentView(btn)
    }

    /**
     * 创建resources
     *
     * @param assetManager
     * @return
     */
    private fun createResources(assetManager: AssetManager): Resources {
        val superRes = resources
        return Resources(assetManager, superRes.displayMetrics, superRes.configuration)
    }
}
