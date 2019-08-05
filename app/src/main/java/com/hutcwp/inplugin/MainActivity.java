package com.hutcwp.inplugin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.hutcwp.small.plugin.PluginManager;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getResources();

    }

    public void startService1InPlugin1(View view) {
//        try {
//            Intent intent = new Intent();
//            String serviceName = PluginManager.mPluginRecords.get(0).getPackageInfo().packageName + ".TestService1";
//            intent.setClass(this, Class.forName(serviceName));
//            startService(intent);
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
    }

    public void startActivityInPlugin1(View view) {
        try {
            Intent intent = new Intent();
            String activityName = "com.hutcwp.pluginb.PluginBMainActivity";
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}