package com.hutcwp.inplugin

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log

class Main2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
    }

    override fun onResume() {
        super.onResume()
        Log.i("test", "onResume")
    }

}
