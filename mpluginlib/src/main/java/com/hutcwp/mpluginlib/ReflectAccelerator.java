/*
 * Copyright 2015-present wequick.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.hutcwp.mpluginlib;

import android.content.res.AssetManager;
import android.os.Build;
import android.util.ArrayMap;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * This class consists exclusively of static methods that accelerate reflections.
 */
public class ReflectAccelerator {
    private static final String TAG = "ReflectAccelerator";

    // ui thread init
    public static Class<?> sActivitythread_class;

    public static Method sActivitythread_currentActivityThread;

    public static Field sActivityThread_mInstrumentation;
    public static Field sActivity_mInstrumentation;
    public static Field sActivityThread_mH;
    public static Field sHandler_mCallback;

    public static Object sCurrentActivityThread;

    // lazy init
    public static Method sAssetManager_addAssetPath_method;
    public static Method sAssetManager_addAssetPaths_method;
    public static Method sAssetManager_getAssetPath_method;
    public static Method sAssetManager_ensureStringBlocks_method;
    public static Method sResourcesManager_getInstance_method;

    public static Field sActivityThread_mActivities;
    public static Field sActivityClientRecord_activity;
    public static Field sActivityThread_mActiveResources;
    public static Field sResourcesManager_mActiveResource;
    public static Field sResourcesManager_mResourceReferences;
    public static Field sActivityThread_mPackages;
    public static Field sContextImpl_mTheme;


    public static Field sContextThemeWrapper_mTheme;
    public static Field sContextThemeWrapper_mResources;

    public static Field sResources_mAssets;
    public static Field sResources_mResourcesImpl;

    public static Field sResources_mTypedArrayPool;

    public static Field sActivityClientRecord_intent_field;
    public static Field sActivityClientRecord_activityInfo_field;

    // ClientTransaction
    private static Field sClientTransaction_mActivityCallbacks_field;  // since Android P

    private static boolean sResHookIsInited = false;

    private static AssetManager sAssetManager;
    private static List<String> sAssetPathList;

    private static ArrayMap<Object, WeakReference<Object>> sResourceImpls;
    private static Object/*ResourcesImpl*/ sMergedResourcesImpl;

    private static final String SELECT_RUNTIME_PROPERTY = "persist.sys.dalvik.vm.lib";
    private static final String LIB_DALVIK = "libdvm.so";
    private static final String LIB_ART = "libart.so";
    private static final String LIB_ART_D = "libartd.so";

    private ReflectAccelerator() { /** cannot be instantiated */}

    private static <T> T invoke(Method method, Object target, Object... args) {
        try {
            return (T) method.invoke(target, args);
        } catch (Exception e) {
            // Ignored
            Log.e(TAG, "invoke  object[" + target + "] method[" + method + "] failed");

            return null;
        }
    }

    private static Method getMethod(Class cls, String methodName, Class[] types) {
        try {
            Method method = cls.getMethod(methodName, types);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "get method [" + cls.getName() + "." + methodName + "] failed");

            return null;
        }
    }

    public static AssetManager newAssetManager() {
        AssetManager assets;
        try {
            assets = AssetManager.class.newInstance();
        } catch (InstantiationException e1) {
            e1.printStackTrace();
            return null;
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
            return null;
        }
        return assets;
    }

    public static AssetManager getAssetManager() {
        return sAssetManager;
    }

    public static int addAssetPath(AssetManager assets, String path) {
        sAssetManager_addAssetPath_method = getMethod(AssetManager.class, "addAssetPath", new Class[]{String.class});

        if (sAssetManager_addAssetPath_method == null) return 0;
        Integer ret = invoke(sAssetManager_addAssetPath_method, assets, path);
        if (ret == null) return 0;
        return ret;
    }

    public static String getAssetPath(AssetManager assets, int index) {
        String ret = null;
        if (assets != null) {
            ret = invoke(sAssetManager_getAssetPath_method, assets, index);
        }
        return ret;
    }

    public static int[] addAssetPaths(AssetManager assets, String[] paths) {
        if (Build.VERSION.SDK_INT < 28) {
            if (sAssetManager_addAssetPaths_method == null) return null;

            return invoke(sAssetManager_addAssetPaths_method, assets, new Object[]{paths});
        } else {
            int N = paths.length;
            int[] ids = new int[N];
            for (int i = 0; i < N; i++) {
                ids[i] = addAssetPath(assets, paths[i]);
            }
            return ids;
        }
    }


    public static Field getDeclaredField(Class cls, String fieldName) {
        try {
            Field field = cls.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "get field [" + cls.getName() + "." + fieldName + "] failed");

            return null;
        }
    }


}
