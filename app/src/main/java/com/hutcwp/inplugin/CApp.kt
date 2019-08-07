package com.hutcwp.inplugin

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.hutcwp.small.Small
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.runtime.Permission

/**
 * 自定义Application
 */
class CApp : Application() {

    companion object {
        const val TAG = "CApp"
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
        Log.i(TAG, "attachBaseContext")
        if (!permissionGrant()) {
            Toast.makeText(this, "外存读写权限未授予,请授予", Toast.LENGTH_LONG).show()
            AndPermission.with(this)
                .runtime()
                .permission(*Permission.Group.STORAGE)
                .onGranted {
                    // Storage permission are allowed.
                    Log.i(TAG, "onAllowed")
                    Small.preSetUp(this)
                    Small.setUp({
                        if (it == Small.SetupResult.PluginSetupSuccess) {
                            Log.i(TAG, "set up success.")
                        } else if (it == Small.SetupResult.PluginSetupFail) {
                            Log.e(TAG, "set up fail.")
                        }
                    }, true)
                }
                .onDenied {
                    // Storage permission are not allowed.
                    // todo 插件放置在存储卡中，如果没有权限插件无法加载。
                    Log.i(TAG, "onDenied")
                    Toast.makeText(this, "未授权，插件加载失败。", Toast.LENGTH_LONG).show()
                }
                .start()
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate")
        if (permissionGrant()) {
            Small.preSetUp(this)
            Small.setUp({
                if (it == Small.SetupResult.PluginSetupSuccess) {
                    Log.i(TAG, "set up success.")
                } else if (it == Small.SetupResult.PluginSetupFail) {
                    Log.e(TAG, "set up fail.")
                }
            }, true)
        }
    }

    private fun permissionGrant(): Boolean {
        val hasStoragePermissions = AndPermission.hasPermissions(this, Permission.WRITE_EXTERNAL_STORAGE)
        return hasStoragePermissions
    }

}
