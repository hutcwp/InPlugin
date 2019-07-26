package com.hutcwp.inplugin;

import android.app.Application;
import android.content.Context;
import com.hutcwp.mpluginlib.PluginManager;


public class MyApplication extends Application {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        PluginManager.init(this);
    }

}
