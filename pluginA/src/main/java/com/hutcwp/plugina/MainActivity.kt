package com.hutcwp.plugina

import android.os.Bundle
import android.util.Log
import com.hutcwp.mpluginlib.ZeusBaseActivity

class MainActivity : ZeusBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("test", "plugin MainActivity onCreate")
        setContentView(R.layout.activity_main_plugin)
        val pluginLayout = resources.getLayout(R.layout.activity_main_plugin)
    }
}
