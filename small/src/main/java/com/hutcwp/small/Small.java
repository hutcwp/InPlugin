package com.hutcwp.small;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
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

    private static boolean hasSetUp = false;
    private static volatile Resources mNowResources;
    private static volatile Application mApp;


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

    /**
     * 组件激活回调
     */
    public enum ActivePluginResult {
        PluginActiveSuccess,
        PluginActiveFail,
    }

    public interface OnActiveListener {
        void onActive(ActivePluginResult result);
    }

    public static Context getContext() {
        return mApp;
    }

    public static Resources getResources() {
        return mNowResources;
    }

    public static void setNowResources(Resources mNowResources) {
        Small.mNowResources = mNowResources;
    }

    private static void init(Application application) {
        hasSetUp = true;
        mApp = application;
        mNowResources = mApp.getResources();
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

        if (PluginManager.INSTANCE.setup(mApp)) {
            hasSetUp = true;
            if (listener != null) {
                listener.onSetup(SetupResult.PluginSetupSuccess);
            }

            PluginManager.INSTANCE.loadSetupPlugins();
        } else {
            listener.onSetup(SetupResult.PluginSetupFail);
        }
    }

    public static void activeSinglePlugin(String pluginId, OnActiveListener listener) {
        if (!PluginManager.INSTANCE.pluginEnable(pluginId)) {
            loadPlugin(pluginId);
        }

        PluginManager.INSTANCE.activeSinglePlugin(pluginId, listener);
    }

    public static void loadPlugin(String pluginId) {
        PluginManager.INSTANCE.loadSinglePlugin(pluginId);
    }

}
