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

package com.hutcwp.small.util;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.ArrayMap;
import android.util.Log;
import android.view.ContextThemeWrapper;
import com.hutcwp.small.Small;
import com.hutcwp.small.logging.Logging;


import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.*;
import java.util.*;
import java.util.zip.ZipFile;

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

    public static Object mPackageInfo = null;

    private static ArrayMap<Object, WeakReference<Object>> sResourceImpls;
    private static Object/*ResourcesImpl*/ sMergedResourcesImpl;

    private static final String SELECT_RUNTIME_PROPERTY = "persist.sys.dalvik.vm.lib";
    private static final String LIB_DALVIK = "libdvm.so";
    private static final String LIB_ART = "libart.so";
    private static final String LIB_ART_D = "libartd.so";

    private ReflectAccelerator() { /** cannot be instantiated */}

    public static String currentProcessName(Application context) {
        String curProcessName = null;
        try {
            sActivitythread_class = Class.forName("android.app.ActivityThread");
            Method activitythread_currentProcessName = sActivitythread_class.getMethod("currentProcessName", new Class[0]);
            activitythread_currentProcessName.setAccessible(true);
            curProcessName = (String) activitythread_currentProcessName.invoke(null, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (curProcessName == null) {
            curProcessName = getProcessName(context);
        }
        return curProcessName;
    }


    public static String getProcessName(Application context) {
        try {
            Field mLoadedApk = context.getClass().getField("mLoadedApk");
            mLoadedApk.setAccessible(true);
            Object apk = mLoadedApk.get(context);
            Field mActivityThreadField = apk.getClass().getDeclaredField("mActivityThread");
            mActivityThreadField.setAccessible(true);
            sCurrentActivityThread = mActivityThreadField.get(apk);
            Method activitythread_getProcessName = sActivitythread_class.getMethod("getProcessName", new Class[0]);
            activitythread_getProcessName.setAccessible(true);
            return (String) activitythread_getProcessName.invoke(sCurrentActivityThread, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean init(Application context) {
        try {
            if (sActivitythread_class == null) {
                sActivitythread_class = Class.forName("android.app.ActivityThread");
            }

            sActivitythread_currentActivityThread = sActivitythread_class.getMethod("currentActivityThread", new Class[0]);
            sActivitythread_currentActivityThread.setAccessible(true);

            if (sCurrentActivityThread == null) {
                sCurrentActivityThread = sActivitythread_currentActivityThread.invoke(null, new Object[0]);
            }
            if (sCurrentActivityThread == null) {
                Field mLoadedApk = context.getClass().getField("mLoadedApk");
                mLoadedApk.setAccessible(true);
                Object apk = mLoadedApk.get(context);
                Field mActivityThreadField = apk.getClass().getDeclaredField("mActivityThread");
                mActivityThreadField.setAccessible(true);

                sCurrentActivityThread = mActivityThreadField.get(apk);
            }

            sActivityThread_mInstrumentation = getDeclaredField(sActivitythread_class, "mInstrumentation");
            sActivity_mInstrumentation = getDeclaredField(Activity.class, "mInstrumentation");
            sActivityThread_mH = getDeclaredField(sActivitythread_class, "mH");
            sHandler_mCallback = getDeclaredField(Handler.class, "mCallback");
            mPackageInfo = RefInvoke.getFieldObject(context, "mPackageInfo");

        } catch (Exception e) {
            e.printStackTrace();
            Logging.error(TAG, "", e);
        }

        return true;
    }

    private static final class V9_13 {

        private static Field sPathClassLoader_libraryPathElements_field;


        public static void expandNativeLibraryDirectories(ClassLoader classLoader,
                                                          List<File> libPaths, boolean push) {
            if (sPathClassLoader_libraryPathElements_field == null) {
                sPathClassLoader_libraryPathElements_field = getDeclaredField(
                        classLoader.getClass(), "libraryPathElements");
            }
            List<String> paths = getValue(sPathClassLoader_libraryPathElements_field, classLoader);
            if (paths == null) return;
            for (File libPath : libPaths) {
                if (push) {
                    paths.add(0, libPath.getAbsolutePath() + File.separator);
                } else {
                    paths.add(libPath.getAbsolutePath() + File.separator);
                }
            }
        }
    }

    private static class V14_ { // API 14 and upper

        // DexPathList
        protected static Field sPathListField;
        private static Constructor sDexElementConstructor;
        private static Class sDexElementClass;

        protected static Object makeDexElement(File dir) throws Exception {
            final int v = Build.VERSION.SDK_INT;
            if (v < 26) {
                return makeDexElement(dir, true, null);
            } else {
                return makeDexElementV26(dir);
            }
        }

        private static Object makeDexElement(File pkg, boolean isDirectory, DexFile dexFile) throws Exception {
            if (sDexElementClass == null) {
                sDexElementClass = Class.forName("dalvik.system.DexPathList$Element");
            }
            if (sDexElementConstructor == null) {
                sDexElementConstructor = sDexElementClass.getConstructors()[0];
            }
            Class<?>[] types = sDexElementConstructor.getParameterTypes();
            switch (types.length) {
                case 3:
                    if (types[1].equals(ZipFile.class)) {
                        // Element(File apk, ZipFile zip, DexFile dex)
                        ZipFile zip;
                        try {
                            zip = new ZipFile(pkg);
                        } catch (IOException e) {
                            throw e;
                        }
                        try {
                            return sDexElementConstructor.newInstance(pkg, zip, dexFile);
                        } catch (Exception e) {
                            zip.close();
                            throw e;
                        }
                    } else {
                        // Element(File apk, File zip, DexFile dex)
                        return sDexElementConstructor.newInstance(pkg, pkg, dexFile);
                    }
                case 4:
                default:
                    // Element(File apk, boolean isDir, File zip, DexFile dex)
                    if (isDirectory) {
                        return sDexElementConstructor.newInstance(pkg, true, null, null);
                    } else {
                        return sDexElementConstructor.newInstance(pkg, false, pkg, dexFile);
                    }
            }
        }

        protected static Object makeDexElementV26(File dir) throws Exception {
            if (sDexElementClass == null) {
                sDexElementClass = Class.forName("dalvik.system.DexPathList$NativeLibraryElement");
            }

            if (sDexElementConstructor == null) {
                Constructor<?>[] constructors = sDexElementClass.getConstructors();
                for (Constructor constructor : constructors) {
                    Class<?>[] types = constructor.getParameterTypes();
                    if (types.length == 1 && types[0] == dir.getClass()) {
                        sDexElementConstructor = constructor;
                        sDexElementConstructor.setAccessible(true);
                    }
                }
            }
            if (sDexElementConstructor != null) {
                return sDexElementConstructor.newInstance(dir);
            } else {
                throw new Exception("Can't found dalvik.system.DexPathList$NativeLibraryElement constructor(File)");
            }
        }
    }

    private static final class V9_20 {

        private static Method sInstrumentation_execStartActivityV20_method;

        public static Instrumentation.ActivityResult execStartActivity(
                Instrumentation instrumentation, Context who, IBinder contextThread, IBinder token,
                Activity target, Intent intent, int requestCode) throws InvocationTargetException, IllegalAccessException {
            if (sInstrumentation_execStartActivityV20_method == null) {
                Class[] types = new Class[]{Context.class, IBinder.class, IBinder.class,
                        Activity.class, Intent.class, int.class};
                sInstrumentation_execStartActivityV20_method = getMethod(Instrumentation.class,
                        "execStartActivity", types);
            }
            if (sInstrumentation_execStartActivityV20_method == null) return null;
            return (Instrumentation.ActivityResult) sInstrumentation_execStartActivityV20_method.invoke(instrumentation,
                    who, contextThread, token, target, intent, requestCode);
        }
    }

    private static final class V21_ {

        private static Method sInstrumentation_execStartActivityV21_method;

        public static Instrumentation.ActivityResult execStartActivity(
                Instrumentation instrumentation, Context who, IBinder contextThread, IBinder token,
                Activity target, Intent intent, int requestCode, Bundle options) throws InvocationTargetException, IllegalAccessException {
            if (sInstrumentation_execStartActivityV21_method == null) {
                Class[] types = new Class[]{Context.class, IBinder.class, IBinder.class,
                        Activity.class, Intent.class, int.class, android.os.Bundle.class};
                sInstrumentation_execStartActivityV21_method = getMethod(Instrumentation.class,
                        "execStartActivity", types);
            }
            if (sInstrumentation_execStartActivityV21_method == null) return null;
            return (Instrumentation.ActivityResult) sInstrumentation_execStartActivityV21_method.invoke(instrumentation,
                    who, contextThread, token, target, intent, requestCode, options);
        }
    }

    private static class V14_22 extends V14_ {

        protected static Field sDexPathList_nativeLibraryDirectories_field;

        public static void expandNativeLibraryDirectories(ClassLoader classLoader,
                                                          List<File> libPaths, boolean push) {

            if (sPathListField == null) {
                sPathListField = getDeclaredField(DexClassLoader.class.getSuperclass(), "pathList");
            }

            if (sPathListField == null) {
                return;
            }

            Object pathList = getValue(sPathListField, classLoader);
            if (pathList == null) return;

            if (sDexPathList_nativeLibraryDirectories_field == null) {
                sDexPathList_nativeLibraryDirectories_field = getDeclaredField(
                        pathList.getClass(), "nativeLibraryDirectories");
                if (sDexPathList_nativeLibraryDirectories_field == null) return;
            }

            try {
                // File[] nativeLibraryDirectories
                Object[] paths = libPaths.toArray();
                expandArray(pathList, sDexPathList_nativeLibraryDirectories_field, paths, push);
            } catch (Exception e) {
                Logging.error(TAG, "", e);
            }
        }
    }

    private static final class V23_ extends V14_22 {

        private static Field sDexPathList_nativeLibraryPathElements_field;

        public static void expandNativeLibraryDirectories(ClassLoader classLoader,
                                                          List<File> libPaths, boolean push) {
            if (sPathListField == null) {
                sPathListField = getDeclaredField(DexClassLoader.class.getSuperclass(), "pathList");
            }

            if (sPathListField == null) {
                return;
            }

            Object pathList = getValue(sPathListField, classLoader);
            if (pathList == null) return;

            if (sDexPathList_nativeLibraryDirectories_field == null) {
                sDexPathList_nativeLibraryDirectories_field = getDeclaredField(
                        pathList.getClass(), "nativeLibraryDirectories");
                if (sDexPathList_nativeLibraryDirectories_field == null) return;
            }

            try {
                // List<File> nativeLibraryDirectories
                List<File> paths = getValue(sDexPathList_nativeLibraryDirectories_field, pathList);
                if (paths == null) return;
                if (push) {
                    paths.addAll(0, libPaths);
                } else {
                    paths.addAll(libPaths);
                }

                // Element[] nativeLibraryPathElements
                if (sDexPathList_nativeLibraryPathElements_field == null) {
                    sDexPathList_nativeLibraryPathElements_field = getDeclaredField(
                            pathList.getClass(), "nativeLibraryPathElements");
                }
                if (sDexPathList_nativeLibraryPathElements_field == null) return;

                int N = libPaths.size();
                Object[] elements = new Object[N];
                for (int i = 0; i < N; i++) {
                    Object dexElement = makeDexElement(libPaths.get(i));
                    elements[i] = dexElement;
                }

                expandArray(pathList, sDexPathList_nativeLibraryPathElements_field, elements, push);
            } catch (Exception e) {
                Logging.error(TAG, "", e);
            }
        }
    }

    //______________________________________________________________________________________________
    // API

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

    private static void updateResourceAssert(Resources resources, AssetManager ass) {
        if (ass == null) {
            Logging.error(TAG, "updateResourceAssert ass is null");
            return;
        }
        try {
            try {
                sResources_mAssets.set(resources, ass);
            } catch (Throwable ignore) {
                Logging.error(TAG, "updateResourceAssert failed err = " + ignore.getMessage());

                Object resourceImpl = sResources_mResourcesImpl.get(resources);
                Field implAssets = findField(resourceImpl, "mAssets");
                if (implAssets != null) {
                    implAssets.setAccessible(true);
                    implAssets.set(resourceImpl, ass);
                }

                if (Build.VERSION.SDK_INT >= 24) {
                    if (resources == Small.getContext().getResources()) {
                        sMergedResourcesImpl = resourceImpl;
                    }
                }
            }

            resources.updateConfiguration(resources.getConfiguration(), resources.getDisplayMetrics());
        } catch (Throwable e) {
            e.printStackTrace();
            Logging.error(TAG, "updateResourceAssert failed err = " + e.getMessage());
            //    throw new IllegalStateException(e);
        }
    }

    public static void updateResourceAssert(Resources resources) {
        updateResourceAssert(resources, sAssetManager);
    }

    private static void updateResourceTypePool(Resources resources) {
        try {
            // android.util.Pools$SynchronizedPool<TypedArray>
            Object typedArrayPool = sResources_mTypedArrayPool.get(resources);
            // Clear all the pools
            Method acquire = typedArrayPool.getClass().getMethod("acquire");
            acquire.setAccessible(true);
            while (acquire.invoke(typedArrayPool) != null) ;
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean getIsArtInUse() {
        final String vmVersion = System.getProperty("java.vm.version");
        return vmVersion != null && vmVersion.startsWith("2");
    }

    private static CharSequence getCurrentRuntimeValue() {
        try {
            Class<?> systemProperties = Class.forName("android.os.SystemProperties");
            try {
                Method get = systemProperties.getMethod("get",
                        String.class, String.class);
                if (get == null) {
                    return "WTF?!";
                }
                try {
                    final String value = (String) get.invoke(
                            systemProperties, SELECT_RUNTIME_PROPERTY,
                            /* Assuming default is */"Dalvik");
                    if (LIB_DALVIK.equals(value)) {
                        return "Dalvik";
                    } else if (LIB_ART.equals(value)) {
                        return "ART";
                    } else if (LIB_ART_D.equals(value)) {
                        return "ART debug build";
                    }

                    return value;
                } catch (IllegalAccessException e) {
                    return "IllegalAccessException";
                } catch (IllegalArgumentException e) {
                    return "IllegalArgumentException";
                } catch (InvocationTargetException e) {
                    return "InvocationTargetException";
                }
            } catch (NoSuchMethodException e) {
                return "SystemProperties.get(String key, String def) method is not found";
            }
        } catch (ClassNotFoundException e) {
            return "SystemProperties class is not found";
        }
    }

    public static void mergeResources(Application app, String[] assetPaths) {
        lazyInit(app);

//        for (String path : assetPaths) {
////            JniHooker.addPluginResourcePath(path);
////        }

        updateAssetManager(app, assetPaths);
        updateResource(app);
    }

    public static void updateResource(Application app) {
        if (!sResHookIsInited) {
            return;
        }

        try {
            if (Build.VERSION.SDK_INT < 28) {
                sAssetManager_ensureStringBlocks_method.invoke(sAssetManager, new Object[0]);
            }

            Object referencesLock = null;
            Collection<WeakReference<Resources>> references;
            Collection<WeakReference<Activity>> activities = new ArrayList<>();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Object resourcesManager = sResourcesManager_getInstance_method.invoke(null, new Object[0]);
                if (Build.VERSION.SDK_INT >= 24) {
                    referencesLock = resourcesManager;
                }

                if (sResourcesManager_mActiveResource != null) {
                    Map<?, WeakReference<Resources>> arrayMap = (ArrayMap) sResourcesManager_mActiveResource.get(resourcesManager);
                    references = arrayMap.values();
                } else {
                    references = (Collection) sResourcesManager_mResourceReferences.get(resourcesManager);
                }
            } else {
                HashMap<?, WeakReference<Resources>> map = (HashMap) sActivityThread_mActiveResources.get(sCurrentActivityThread);
                referencesLock = sActivityThread_mPackages.get(sCurrentActivityThread);
                references = map.values();
            }

            Map<?, ?> activitesMap = (Map) sActivityThread_mActivities.get(sCurrentActivityThread);
            for (Object ac : activitesMap.values()) {
                activities.add(new WeakReference<>((Activity) sActivityClientRecord_activity.get(ac)));
            }

            if (referencesLock != null) {
                synchronized (referencesLock) {
                    for (WeakReference<Resources> wr : references) {
                        Resources resources = wr.get();
                        if (resources == null) continue;
                        updateResourceAssert(resources, sAssetManager);
                    }
                }
            } else {
                for (WeakReference<Resources> wr : references) {
                    Resources resources = wr.get();
                    if (resources == null) continue;
                    updateResourceAssert(resources, sAssetManager);
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (referencesLock != null) {
                    synchronized (referencesLock) {
                        for (WeakReference<Resources> wr : references) {
                            if (wr == null) continue;
                            Resources resources = wr.get();
                            if (resources == null) continue;
                            updateResourceTypePool(resources);
                        }
                    }
                } else {
                    for (WeakReference<Resources> wr : references) {
                        if (wr == null) continue;
                        Resources resources = wr.get();
                        if (resources == null) continue;
                        updateResourceTypePool(resources);
                    }
                }
            }

            sContextImpl_mTheme.set(app.getBaseContext(), null);

            List<WeakReference<Activity>> activityList = ActivityTaskMgr.INSTANCE.getActivityList();
            for (WeakReference<Activity> ac : activityList) {
                activities.add(ac);
            }

            Set<String> activityNameList = new HashSet<>();
            for (WeakReference<Activity> ac : activities) {
                if (ac == null || ac.get() == null) {
                    continue;
                }

                String name = ac.get().getClass().getName();
                if (activityNameList.contains(name)) {
                    continue;
                }

                activityNameList.add(name);

                sContextThemeWrapper_mTheme.set(ac.get(), null);
                if (sContextThemeWrapper_mResources != null) {
                    Resources re = (Resources) sContextThemeWrapper_mResources.get(ac.get());
                    updateResourceAssert(re, sAssetManager);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        updateResourceTypePool(re);
                    }
                }
            }

        } catch (Throwable e) {
            e.printStackTrace();
            Logging.error(TAG, "updateResource failed", e);
//            StatisticsUtils.updateResourceFailure("updateResource", Log.getStackTraceString(e));
        }
    }

    private static void updateAssetManager(Application app, String[] assetPaths) {
        // In sdk < 19;we should create new asset manager
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            sAssetManager = null;
        }

        long before = System.currentTimeMillis();
        if (sAssetManager == null) {
            if (Build.VERSION.SDK_INT >= 24) {
                try {
                    new android.webkit.WebView(app);
                } catch (Throwable e) {
                    Logging.error(TAG, "new webview field", e);
                }

                sAssetManager = app.getAssets();
            } else {
                sAssetManager = newAssetManager();
            }
            if (sAssetPathList != null) {
                for (String path : sAssetPathList) {
                    addAssetPath(sAssetManager, path);
                }
            }
        }

        long after = System.currentTimeMillis();
        Logging.info(TAG, "new asset manager take time %d", after - before);
        before = after;

        if (sAssetPathList == null) {
            sAssetPathList = new ArrayList<>();
            addAssetPaths(sAssetManager, assetPaths);
            sAssetPathList.addAll(Arrays.asList(assetPaths));
        } else if (assetPaths != null) {
            for (String path : assetPaths) {
                if (!sAssetPathList.contains(path)) {
                    addAssetPath(sAssetManager, path);
                    sAssetPathList.add(path);
                }
            }
        }

        after = System.currentTimeMillis();
        Logging.info(TAG, "add path to asset manager take time %d", after - before);
    }

    public static void expandNativeLibraryDirectories(ClassLoader classLoader, List<File> libPath, boolean push) {
        final int v = Build.VERSION.SDK_INT;
        if (v < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            V9_13.expandNativeLibraryDirectories(classLoader, libPath, push);
        } else if (v < Build.VERSION_CODES.M) {
            V14_22.expandNativeLibraryDirectories(classLoader, libPath, push);
        } else {
            V23_.expandNativeLibraryDirectories(classLoader, libPath, push);
        }
    }

    public static Instrumentation.ActivityResult execStartActivity(
            Instrumentation instrumentation,
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, android.os.Bundle options) throws InvocationTargetException, IllegalAccessException {
        return V21_.execStartActivity(instrumentation,
                who, contextThread, token, target, intent, requestCode, options);
    }

    public static Instrumentation.ActivityResult execStartActivity(
            Instrumentation instrumentation,
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode) throws InvocationTargetException, IllegalAccessException {
        return V9_20.execStartActivity(instrumentation,
                who, contextThread, token, target, intent, requestCode);
    }

    public static Intent getIntent(Object/*ActivityClientRecord*/ r) {
        if (sActivityClientRecord_intent_field == null) {
            sActivityClientRecord_intent_field = getDeclaredField(r.getClass(), "intent");
        }
        return getValue(sActivityClientRecord_intent_field, r);
    }

    public static void setActivityInfo(Object/*ActivityClientRecord*/ r, ActivityInfo ai) {
        if (sActivityClientRecord_activityInfo_field == null) {
            sActivityClientRecord_activityInfo_field = getDeclaredField(
                    r.getClass(), "activityInfo");
        }
        setValue(sActivityClientRecord_activityInfo_field, r, ai);
    }

    public static void resetResourcesAndTheme(Activity activity, int themeId) {
        AssetManager newAssetManager = activity.getApplication().getAssets();
        Resources resources = activity.getResources();

        if (null == newAssetManager) {
            Logging.info(TAG, "resetResourcesAndTheme but application AssetManager is null");
            return;
        }
        // Set the activity resources assets to the application one
        try {
            Field mResourcesImpl = Resources.class.getDeclaredField("mResourcesImpl");
            mResourcesImpl.setAccessible(true);
            Object resourceImpl = mResourcesImpl.get(resources);
            Field implAssets = resourceImpl.getClass().getDeclaredField("mAssets");
            implAssets.setAccessible(true);
            implAssets.set(resourceImpl, newAssetManager);
        } catch (Throwable e) {
            android.util.Log.e("Small", "Failed to update resources for activity " + activity, e);
        }

        // Reset the theme
        try {
            Field mt = ContextThemeWrapper.class.getDeclaredField("mTheme");
            mt.setAccessible(true);
            mt.set(activity, null);
        } catch (Throwable e) {
            android.util.Log.e("Small", "Failed to update existing theme for activity " + activity, e);
        }

        activity.setTheme(themeId);
    }

    //______________________________________________________________________________________________
    // Private

    /**
     * Add elements to Object[] with reflection
     *
     * @param target
     * @param arrField
     * @param extraElements
     * @param push          true=push to array head, false=append to array tail
     * @throws IllegalAccessException
     * @see <a href="https://github.com/casidiablo/multidex/blob/publishing/library/src/android/support/multidex/MultiDex.java">MultiDex</a>
     */
    private static void expandArray(Object target, Field arrField,
                                    Object[] extraElements, boolean push)
            throws IllegalAccessException {
        Object[] original = (Object[]) arrField.get(target);
        Object[] combined = (Object[]) Array.newInstance(
                original.getClass().getComponentType(), original.length + extraElements.length);
        if (push) {
            System.arraycopy(extraElements, 0, combined, 0, extraElements.length);
            System.arraycopy(original, 0, combined, extraElements.length, original.length);
        } else {
            System.arraycopy(original, 0, combined, 0, original.length);
            System.arraycopy(extraElements, 0, combined, original.length, extraElements.length);
        }
        arrField.set(target, combined);
    }

    private static void sliceArray(Object target, Field arrField, int deleteIndex)
            throws IllegalAccessException {
        Object[] original = (Object[]) arrField.get(target);
        if (original.length == 0) return;

        Object[] sliced = (Object[]) Array.newInstance(
                original.getClass().getComponentType(), original.length - 1);
        if (deleteIndex > 0) {
            // Copy left elements
            System.arraycopy(original, 0, sliced, 0, deleteIndex);
        }
        int rightCount = original.length - deleteIndex - 1;
        if (rightCount > 0) {
            // Copy right elements
            System.arraycopy(original, deleteIndex + 1, sliced, deleteIndex, rightCount);
        }
        arrField.set(target, sliced);
    }

    private static Method getDeclaredMethod(Class cls, String methodName, Class[] types) {
        try {
            Method method = cls.getDeclaredMethod(methodName, types);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            Logging.error(TAG, "get method [" + cls.getName() + "." + methodName + "] failed");

            return null;
        }
    }

    private static Method getMethod(Class cls, String methodName, Class[] types) {
        try {
            Method method = cls.getMethod(methodName, types);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            Logging.error(TAG, "get method [" + cls.getName() + "." + methodName + "] failed");

            return null;
        }
    }

    private static Field getDeclaredField(Class cls, String fieldName) {
        try {
            Field field = cls.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            Logging.error(TAG, "get field [" + cls.getName() + "." + fieldName + "] failed");

            return null;
        }
    }

    private static <T> T invoke(Method method, Object target, Object... args) {
        try {
            return (T) method.invoke(target, args);
        } catch (Exception e) {
            // Ignored
            Logging.error(TAG, "invoke  object[" + target + "] method[" + method + "] failed");

            return null;
        }
    }

    private static <T> T getValue(Field field, Object target) {
        try {
            return (T) field.get(target);
        } catch (IllegalAccessException e) {
            Logging.error(TAG, "get value  object[" + target + "] field[" + field + "] failed");

            return null;
        }
    }

    private static void setValue(Field field, Object target, Object value) {
        try {
            field.set(target, value);
        } catch (IllegalAccessException e) {
            // Ignored
            Logging.error(TAG, "set value  object[" + target + "] field[" + field + "] failed");
        }
    }

    /**
     * Locates a given field anywhere in the class inheritance hierarchy.
     *
     * @param instance an object to search the field into.
     * @param name     field name
     * @return a field object
     * @throws NoSuchFieldException if the field cannot be located
     */
    public static Field findField(Object instance, String name) throws NoSuchFieldException {
        for (Class<?> clazz = instance.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Field field = clazz.getDeclaredField(name);

                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                return field;
            } catch (NoSuchFieldException e) {
                // ignore and search next
            }
        }

        throw new NoSuchFieldException("Field " + name + " not found in " + instance.getClass());
    }

    /**
     * Locates a given method anywhere in the class inheritance hierarchy.
     *
     * @param instance       an object to search the method into.
     * @param name           method name
     * @param parameterTypes method parameter types
     * @return a method object
     * @throws NoSuchMethodException if the method cannot be located
     */
    public static Method findMethod(Object instance, String name, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        for (Class<?> clazz = instance.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {

                Method method = clazz.getDeclaredMethod(name, parameterTypes);


                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }

                return method;
            } catch (NoSuchMethodException e) {
                // ignore and search next
            }
        }

        throw new NoSuchMethodException("Method " + name + " with parameters " +
                Arrays.asList(parameterTypes) + " not found in " + instance.getClass());
    }

    public static Instrumentation getHostInstrumentation() throws IllegalAccessException {
        return (Instrumentation) sActivityThread_mInstrumentation.get(sCurrentActivityThread);
    }

    public static void setHostInstrumentation(Instrumentation inst) throws IllegalAccessException {
        sActivityThread_mInstrumentation.set(sCurrentActivityThread, inst);
    }

    public static void setActivityThreadHandlerCallback(Object obj) throws IllegalAccessException {
        Handler ah = (Handler) sActivityThread_mH.get(sCurrentActivityThread);
        ReflectAccelerator.sHandler_mCallback.set(ah, obj);
    }

    public static boolean lazyInit(Application app) {
        if (sResHookIsInited) { // 只初始化一次
            return true;
        }

        sResHookIsInited = true;
        try {
            Class<?> activityClientRecord_class = Class.forName("android.app.ActivityThread$ActivityClientRecord");
            Class<?> contextThemeWrapper_class = Class.forName("android.view.ContextThemeWrapper");
            Class<?> contextImpl_class = Class.forName("android.app.ContextImpl");

            sAssetManager_getAssetPath_method = getMethod(AssetManager.class, "getCookieName", new Class[]{int.class});
            sAssetManager_addAssetPath_method = getMethod(AssetManager.class, "addAssetPath", new Class[]{String.class});

            if (Build.VERSION.SDK_INT < 28) {
                sAssetManager_addAssetPaths_method = getMethod(AssetManager.class, "addAssetPaths", new Class[]{String[].class});
                sAssetManager_ensureStringBlocks_method = getDeclaredMethod(AssetManager.class, "ensureStringBlocks", new Class[0]);
            }

            sActivityThread_mActivities = getDeclaredField(sActivitythread_class, "mActivities");
            sActivityClientRecord_activity = getDeclaredField(activityClientRecord_class, "activity");

            sContextImpl_mTheme = getDeclaredField(contextImpl_class, "mTheme");
            sContextThemeWrapper_mTheme = getDeclaredField(contextThemeWrapper_class, "mTheme");
            sContextThemeWrapper_mResources = getDeclaredField(contextThemeWrapper_class, "mResources");

            sResources_mAssets = getDeclaredField(Resources.class, "mAssets");
            sResources_mResourcesImpl = getDeclaredField(Resources.class, "mResourcesImpl");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                sResources_mTypedArrayPool = getDeclaredField(Resources.class, "mTypedArrayPool");
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Class<?> resourcesManager_class = Class.forName("android.app.ResourcesManager");
                sResourcesManager_getInstance_method = getDeclaredMethod(resourcesManager_class, "getInstance", new Class[0]);

                sResourcesManager_mActiveResource = getDeclaredField(resourcesManager_class, "mActiveResources");
                if (sResourcesManager_mActiveResource == null) {
                    sResourcesManager_mResourceReferences = getDeclaredField(resourcesManager_class, "mResourceReferences");
                }

                if (Build.VERSION.SDK_INT >= 24) {
                    Field fMResourceImpls = resourcesManager_class.getDeclaredField("mResourceImpls");
                    fMResourceImpls.setAccessible(true);

                    Object resourcesManager = sResourcesManager_getInstance_method.invoke(null, new Object[0]);
                    sResourceImpls = (ArrayMap) fMResourceImpls.get(resourcesManager);
                }
            } else {
                sActivityThread_mActiveResources = getDeclaredField(sActivitythread_class, "mActiveResources");
                sActivityThread_mPackages = getDeclaredField(sActivitythread_class, "mPackages");
            }

            Method addAssetPathMethod;
            if (Build.VERSION.SDK_INT >= 24) {
                addAssetPathMethod = getDeclaredMethod(AssetManager.class, "addAssetPathNative", new Class[]{String.class, boolean.class});
            } else {
                addAssetPathMethod = getDeclaredMethod(AssetManager.class, "addAssetPathNative", new Class[]{String.class});
            }
//            String hostAssetPath = app.getPackageResourcePath();
//            JniHooker.setHostResourcePath(hostAssetPath);
//
//            if (JniHooker.isInit) {
//                String info = JniHooker.hookAndroidContentResAssetManagerAddAssetPathNative(addAssetPathMethod, getIsArtInUse());
//                Logging.info(JniHooker.TAG, info);
//            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            String s = Log.getStackTraceString(e);
            Logging.warn(TAG, "lazyInit failed:" + s);
//            StatisticsUtils.updateResourceFailure("lazyInit", s);

            return false;
        }

    }

    public static List/*<LaunchActivityItem>*/ getLaunchActivityItems(Object t) {
        if (sClientTransaction_mActivityCallbacks_field == null) {
            sClientTransaction_mActivityCallbacks_field = getDeclaredField(t.getClass(), "mActivityCallbacks");
        }

        return getValue(sClientTransaction_mActivityCallbacks_field, t);
    }

    public static void setActivityInfoToLaunchActivityItem(Object item, ActivityInfo targetInfo) {
        // The item maybe instance of different classes like
        // `android.app.servertransaction.LaunchActivityItem
        // or `android.app.servertransaction.ActivityConfigurationChangeItem` and etc.
        // So here we cannot cache one reflection field.

        Field f = getDeclaredField(item.getClass(), "mInfo");
        setValue(f, item, targetInfo);
    }

    public static Intent getIntentOfLaunchActivityItem(Object item) {
        // The item maybe instance of different classes like
        // `android.app.servertransaction.LaunchActivityItem`
        // or `android.app.servertransaction.ActivityConfigurationChangeItem` and etc.
        // So here we cannot cache one reflection field.

        Field f = getDeclaredField(item.getClass(), "mIntent");
        return getValue(f, item);
    }

    public static void ensureCacheResources() {
        if (Build.VERSION.SDK_INT < 24) return;

        if (sResourceImpls == null || sMergedResourcesImpl == null) return;

        Set<?> resourceKeys = sResourceImpls.keySet();
        for (Object resourceKey : resourceKeys) {
            WeakReference resourceImpl = (WeakReference) sResourceImpls.get(resourceKey);
            if (resourceImpl != null && resourceImpl.get() != sMergedResourcesImpl) {
                // Sometimes? the weak reference for the key was released by what
                // we can not find the cache resources we had merged before.
                // And the system will recreate a new one which only build with host resources.
                // So we needs to restore the cache. Fix #429.
                // FIXME: we'd better to find the way to KEEP the weak reference.
                sResourceImpls.put(resourceKey, new WeakReference<Object>(sMergedResourcesImpl));
            }
        }
    }
}
