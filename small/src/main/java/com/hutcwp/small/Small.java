package com.hutcwp.small;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import com.hutcwp.small.plugin.PluginManager;
import com.hutcwp.small.util.RefInvoke;
import com.hutcwp.small.util.ReflectAccelerator;

/**
 * Created by hutcwp on 2019-07-29 16:10
 * email: caiwenpeng@yy.com
 * YY: 909076244
 * 随便起的一个名字，类似Small...
 **/
public final class Small {

    private static final String TAG = "Small";

    private static boolean hasSetUp = false;
    public static Object mPackageInfo = null;
    private static Application mContext = null;
    public static volatile Resources mNowResources;

    @SuppressLint("StaticFieldLeak")
    public static volatile Context mBaseContext;


    /**
     * 组件加载完成回调函数
     */
    public enum SetupResult {
        PluginSetupSuccess,
        PluginSetupFail,
    }

    public interface OnSetupListener {
        void onSetup(SetupResult result);
    }


    public static Context getContext() {
        return mContext;
    }

    private static void init(Application application) {
        hasSetUp = true;
        mContext = application;
        mBaseContext = application.getBaseContext();
        mNowResources = mBaseContext.getResources();
        mPackageInfo = RefInvoke.getFieldObject(application.getBaseContext(), "mPackageInfo");
        ReflectAccelerator.init(application);
        ReflectAccelerator.lazyInit(application);
    }

    public static void preSetUp(Application application) {
        Log.i(TAG, "preSetUp");
        init(application);
        PluginManager.INSTANCE.preSetUp(application);
    }

    public static void setUp(final OnSetupListener listener, boolean syncLoad) {
        Log.i(TAG, "setUp");
        if (!hasSetUp) {
            throw new UnsupportedOperationException("you must invoke `preSetUp` method before this!");
        }

        if (PluginManager.INSTANCE.setup(mContext)) {
            PluginManager.INSTANCE.loadSetupPlugins();
            hasSetUp = true;
            if (listener != null) {
                listener.onSetup(SetupResult.PluginSetupSuccess);
            }
        } else {
            listener.onSetup(SetupResult.PluginSetupFail);
        }
    }

    public static void activePlugins() {
        PluginManager.INSTANCE.activePlugin();
    }

}
