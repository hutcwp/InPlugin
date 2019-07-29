package com.hutcwp.mpluginlib.luancher

import android.app.Activity
import android.app.Application
import android.app.Instrumentation
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.util.Log
import com.hutcwp.mpluginlib.plugin.PluginManager
import com.hutcwp.mpluginlib.util.RefInvoke
import com.hutcwp.mpluginlib.util.ReflectAccelerator

/**
 *
 * Created by hutcwp on 2019-07-29 16:38
 * email: caiwenpeng@yy.com
 * YY: 909076244
 *
 **/
class ApkPluginLauncher : PluginLauncher() {

    companion object {
        private const val TAG = "ApkPluginLauncher"
    }

    override fun preSetUp(context: Application?) {
        super.preSetUp(context)
    }

    override fun setUp(context: Context?) {
        super.setUp(context)
        hookInstrumentation()
    }

    override fun postSetUp() {
        super.postSetUp()
    }


    private fun hookInstrumentation() {
        try {
            // 先获取到当前的ActivityThread对象
            val currentActivityThread =
                RefInvoke.getStaticFieldObject("android.app.ActivityThread", "sCurrentActivityThread")
//            val sActivitythread_class = Class.forName("android.app.ActivityThread")
            val host = RefInvoke.getFieldObject(
                currentActivityThread,
                "mInstrumentation"
            ) as Instrumentation
            val mInstrumentationWrapper = InstrumentationWrapper(host)
            RefInvoke.setFieldObject(
                currentActivityThread.javaClass, currentActivityThread,
                "mInstrumentation", mInstrumentationWrapper
            )
        } catch (e: ClassNotFoundException) {
            Log.i(TAG, "hookInstrumentation error.")
            e.printStackTrace()
        }
    }

    class InstrumentationWrapper(var rawInstrumentation: Instrumentation) : Instrumentation() {
        override fun callActivityOnCreate(activity: Activity, icicle: android.os.Bundle) {
            Log.i(TAG, "callActivityOnCreate")
            val activities = PluginManager.plugins[0].pluginParser.packageInfo.activities
            for (ai in activities) {
                if (ai.name == activity.javaClass.name) {
                    Log.i(TAG, "find out activityInfo , name is " + ai.name)
                    applyActivityInfo(activity, ai)
                    break
                }
            }

            rawInstrumentation.callActivityOnCreate(activity, icicle)
        }

        private fun applyActivityInfo(activity: Activity, ai: ActivityInfo) {
            Log.i(TAG, "applyActivityInfo")
            if (Build.VERSION.SDK_INT >= 28) {
                ReflectAccelerator.resetResourcesAndTheme(activity, ai.themeResource)
            }

            val window = activity.window
            window.setSoftInputMode(ai.softInputMode)
            activity.requestedOrientation = ai.screenOrientation
        }
    }
}