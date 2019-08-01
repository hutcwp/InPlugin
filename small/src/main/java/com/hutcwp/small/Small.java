package com.hutcwp.small;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import com.hutcwp.small.plugin.PluginManager;
import com.hutcwp.small.util.ReflectAccelerator;

/**
 * Created by hutcwp on 2019-07-29 16:10
 * email: caiwenpeng@yy.com
 * YY: 909076244
 * 随便起的一个名字，类似Small...
 **/
public final class Small {

    private static final String TAG = "Small";

    private static Application mContext = null;

    public static Context getContext() {
        return mContext;
    }

    public static void preSetUp(Application application) {
        Log.i(TAG, "preSetUp");
        mContext = application;
        ReflectAccelerator.init(application);
        ReflectAccelerator.lazyInit(application);
        PluginManager.init(application);
        PluginManager.INSTANCE.preSetUp(application);
    }

    public static void setUp(Context context) {
        Log.i(TAG, "setUp");
        PluginManager.INSTANCE.setup(context);
    }

    public static void postSetUp() {
        Log.i(TAG, "postSetUp");
    }

}
