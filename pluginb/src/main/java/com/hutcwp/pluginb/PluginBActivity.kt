package com.hutcwp.pluginb

import android.content.res.AssetManager
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import com.hutcwp.mpluginlib.PluginManager
import com.hutcwp.mpluginlib.RefInvoke
import com.hutcwp.mpluginlib.ZeusBaseActivity
import java.util.ArrayList
import java.util.zip.Inflater

class PluginBActivity : ZeusBaseActivity() {

    private var mCurResources: Resources? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("test", "pluginB PluginBActivity onCreate")
        setContentView(R.layout.activity_plugin_b)
//        PluginManager.mNowResources.getLayout(R.layout.activity_plugin_b)

        val layout = mCurResources?.getLayout(R.layout.activity_plugin_b)
        Log.i("test", "layout = $layout")
        val root = ((findViewById<ViewGroup>(android.R.id.content)).getChildAt(0)) as ViewGroup
        layoutInflater.inflate(R.layout.activity_plugin_b, root)
        Log.i("test", "plugin_b = " + PluginManager.mNowResources.getString(R.string.plugin_b))
    }

    private fun reloadInstalledPluginResources(pluginPaths: List<String>): Resources? {
        try {
            val assetManager = AssetManager::class.java.newInstance()
            val addAssetPath = AssetManager::class.java.getMethod("addAssetPath", String::class.java)

            addAssetPath.invoke(assetManager, packageResourcePath)

            for (pluginPath in pluginPaths) {
                addAssetPath.invoke(assetManager, pluginPath)
                Log.i("test", "addAssetPath path = $pluginPath")
            }

            val newResources = Resources(
                assetManager,
                getResources().getDisplayMetrics(),
                getResources().getConfiguration()
            )

            Log.i("test", "newResources = $newResources")
            RefInvoke.setFieldObject(this, "mResources", newResources)
            //这是最主要的需要替换的，如果不支持插件运行时更新，只留这一个就可以了
//            RefInvoke.setFieldObject(mPackageInfo, "mResources", newResources)

//            mResources = newResources
            //需要清理mTheme对象，否则通过inflate方式加载资源会报错
            //如果是activity动态加载插件，则需要把activity的mTheme对象也设置为null
            RefInvoke.setFieldObject(this, "mTheme", null)
            return newResources
        } catch (e: Throwable) {
            Log.i("test", "e=$e")
            e.printStackTrace()
        }
        return null
    }

//    override fun getResources(): Resources {
//        if (mCurResources == null) {
//            Log.i("test", "mResources= null")
//            val paths = listOf(
//                "/data/user/0/com.hutcwp.inplugin/files/plugin1.apk",
//                "/data/user/0/com.hutcwp.inplugin/files/pluginb.apk"
//            )
//            mCurResources = reloadInstalledPluginResources(paths)
//        }
//        return mCurResources!!
//    }
//
}
