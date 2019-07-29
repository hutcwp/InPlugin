package com.hutcwp.inplugin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.hutcwp.mpluginlib.PluginManager;
import com.hutcwp.mpluginlib.hook.AMSHookHelper;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import java.util.List;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AndPermission.with(this)
                .runtime()
                .permission(Permission.Group.STORAGE)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissions) {
                        // Storage permission are allowed.
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissions) {
                        // Storage permission are not allowed.
                    }
                })
                .start();
        getResources();

    }

    public void startService1InPlugin1(View view) {
        try {
            Intent intent = new Intent();
            String serviceName = PluginManager.plugins.get(0).packageInfo.packageName + ".TestService1";
            intent.setClass(this, Class.forName(serviceName));
            startService(intent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void startActivityInPlugin1(View view) {
        try {
            Intent intent = new Intent();
            String activityName = "com.hutcwp.plugina.PluginActivity.PluginActivity";
            intent.setClass(this, Class.forName(activityName));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startActivityInPluginB(View view) {
        try {
            Intent intent = new Intent();
            String activityName = "com.hutcwp.pluginb.PluginBActivity";
            intent.setClass(this, Class.forName(activityName));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        try {
            AMSHookHelper.hookAMN();
            AMSHookHelper.hookActivityThread();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}