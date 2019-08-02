package com.hutcwp.small;

import android.app.Activity;
import android.content.res.Resources;
import com.hutcwp.small.plugin.PluginManager;

/**
 * 基础的activity
 * Created by huangjian on 2016/6/21.
 */
public class ZeusBaseActivity extends Activity {

    @Override
    public Resources getResources() {
        return Small.mNowResources;
    }
}
