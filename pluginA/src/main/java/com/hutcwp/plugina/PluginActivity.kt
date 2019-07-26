package com.hutcwp.plugina

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.hutcwp.mpluginlib.ZeusBaseActivity


class PluginActivity : ZeusBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("test", "pluginA PluginActivity onCreate")
        val btn = Button(this)
        btn.text = "button create a"
        setContentView(btn)

        btn.setOnClickListener {
            Log.i("test", "click button.")
            val intent = Intent()
            intent.setClassName("com.hutcwp.inplugin", "com.hutcwp.inplugin.ActivityA")
            startActivity(intent)
        }

//        setContentView(R.layout.activity_main_plugin)
    }
}