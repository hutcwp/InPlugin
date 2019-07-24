package com.hutcwp.inplugin

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.hutcwp.inplugin.hook.AMSHookHelper

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun click(v: View) {
        val intent = Intent(this, Main2Activity::class.java)
        startActivity(intent)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
        try {
            AMSHookHelper.hookAMN()
            AMSHookHelper.hookActivityThread()
        } catch (e: Throwable) {
            e.printStackTrace()
            Log.i(TAG, "hook error. e = ", e)
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}
