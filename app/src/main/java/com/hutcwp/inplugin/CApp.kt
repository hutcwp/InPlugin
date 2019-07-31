package com.hutcwp.inplugin

import android.app.Application
import android.content.Context
import android.widget.Toast
import com.hutcwp.cow.Small
import com.yanzhenjie.permission.AndPermission
import com.yanzhenjie.permission.runtime.Permission

/**
 * 自定义Application
 */
class CApp : Application() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
        if (permissionGrant()) {
            Small.preSetUp(this)
        } else {
            Toast.makeText(this, "外存读写权限未授予,请授予", Toast.LENGTH_LONG).show()
            AndPermission.with(this)
                .runtime()
                .permission(*Permission.Group.STORAGE)
                .onGranted {
                    // Storage permission are allowed.
                }
                .onDenied {
                    // Storage permission are not allowed.
                }
                .start()
        }
    }

    override fun onCreate() {
        super.onCreate()
        Small.setUp(this)
    }

    private fun permissionGrant(): Boolean {
        val hasStoragePermissions = AndPermission.hasPermissions(this, Permission.WRITE_EXTERNAL_STORAGE)
        return hasStoragePermissions
    }

}
