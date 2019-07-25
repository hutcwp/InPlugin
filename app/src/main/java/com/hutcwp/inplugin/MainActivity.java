package com.hutcwp.inplugin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.hutcwp.mpluginlib.hook.AMSHookHelper;
import com.hutcwp.mpluginlib.PluginManager;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

            String activityName = PluginManager.plugins.get(0).packageInfo.packageName + ".PluginActivity";
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