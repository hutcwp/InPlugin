package com.hutcwp.inplugin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.hutcwp.small.Small;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getResources();

    }

    public void startService1InPlugin1(View view) {
//        Small.loadPlugin("1");
        Small.activeSinglePlugin("1", new Small.OnActiveListener() {
            @Override
            public void onActive(Small.ActivePluginResult result) {
                Log.i("test", "result = " + result);
            }
        });
//        try {
//            Intent intent = new Intent();
//            String serviceName = PluginManager.mPluginRecords.get(0).getPackageInfo().packageName + ".TestService1";
//            intent.setClass(this, Class.forName(serviceName));
//            startService(intent);
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
    }

    public void startActivityInPlugina(View view) {
        try {
            Intent intent = new Intent();
            String activityName = "com.hutcwp.plugina.PluginActivity";
            intent.setClass(this, Class.forName(activityName));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startMainActivityInPluginB(View view) {
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
}