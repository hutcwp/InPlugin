package com.hutcwp.plugina;

import android.util.Log;
import com.hutcwp.small.plugin.IPluginEntryPoint;

/**
 * Created by hutcwp on 2019-08-06 11:38
 * email: caiwenpeng@yy.com
 * YY: 909076244
 **/
public enum PluginEntryPoint implements IPluginEntryPoint {

    INSTANCE;

    @Override
    public void initialize() {
        Log.i("test", "initialize");
    }

    @Override
    public void mainEntry() {
        Log.i("test", "mainEntry");
    }
}