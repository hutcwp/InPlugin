package com.hutcwp.plugina

import android.os.Bundle
import android.util.Log
import com.hutcwp.small.ZeusBaseActivity

class MainActivity : ZeusBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("MainActivity", "plugin MainActivity onCreate")
        setContentView(R.layout.activity_main_plugin)
    }
}
