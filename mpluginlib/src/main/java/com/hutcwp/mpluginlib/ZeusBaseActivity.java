package com.hutcwp.mpluginlib;

import android.app.Activity;
import android.content.res.Resources;

/**
 * 基础的activity
 * Created by huangjian on 2016/6/21.
 */
public class ZeusBaseActivity extends Activity {

    @Override
    public Resources getResources() {
        return PluginManager.mNowResources;
    }
}
