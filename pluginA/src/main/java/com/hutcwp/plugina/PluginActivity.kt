package com.hutcwp.plugina

import android.app.Activity
import android.content.Intent
import android.content.res.AssetManager
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.widget.Button


class PluginActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val btn = Button(this)
        btn.text = "button create"
        setContentView(btn)

        btn.setOnClickListener {
            Log.i("test", "click button.")
            val intent = Intent()
            intent.setClassName("com.hutcwp.inplugin", "com.hutcwp.inplugin.ActivityA")
            startActivity(intent)
        }
    }


}
