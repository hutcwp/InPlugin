package com.hutcwp.plugina

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.hutcwp.small.ZeusBaseActivity


class PluginActivity : ZeusBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "pluginA PluginActivity onCreate")
        val btn = Button(this)
        btn.text = "button create a"
        setContentView(btn)

        btn.setOnClickListener {
            Log.i(TAG, "click button.")
            val intent = Intent()
            intent.setClassName("com.hutcwp.inplugin", "com.hutcwp.inplugin.ActivityA")
            startActivity(intent)
        }
    }

    companion object {
        private const val TAG = "PluginActivity"
    }
}