package com.hutcwp.cow.internal;

/**
 * Created by hutcwp on 2019-07-30 17:35
 * email: caiwenpeng@yy.com
 * YY: 909076244
 **/

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;
import com.hutcwp.cow.util.PluginController;
import com.hutcwp.cow.util.ReflectAccelerator;

/**
 * Class for redirect activity from Stub(AndroidManifest.xml) to Real(Plugin)
 */
public class InstrumentationWrapper extends Instrumentation {

    private static final String TAG = "InstrumentationWrapper";
    private static final int STUB_ACTIVITIES_COUNT = 4;
    private Instrumentation mHostInstrumentation;

    public InstrumentationWrapper(Instrumentation hostInstrumentation) {
        mHostInstrumentation = hostInstrumentation;
    }

//    /**
//     * @Override V21+
//     * Wrap activity from REAL to STUB
//     */
//    public ActivityResult execStartActivity(
//            Context who, IBinder contextThread, IBinder token, Activity target,
//            Intent intent, int requestCode, android.os.Bundle options)
//            throws InvocationTargetException, IllegalAccessException {
//        Intent replaceIntent = wrapIntent(intent);
//        if (replaceIntent != null) {
//            intent = replaceIntent;
//        }
//        return ReflectAccelerator.execStartActivity(mHostInstrumentation,
//                who, contextThread, token, target, intent, requestCode, options);
//    }
//
//    /**
//     * @Override V20-
//     * Wrap activity from REAL to STUB
//     */
//    public ActivityResult execStartActivity(
//            Context who, IBinder contextThread, IBinder token, Activity target,
//            Intent intent, int requestCode) throws InvocationTargetException, IllegalAccessException {
//        Intent replaceIntent = wrapIntent(intent);
//        if (replaceIntent != null) {
//            intent = replaceIntent;
//        }
//        return ReflectAccelerator.execStartActivity(mHostInstrumentation,
//                who, contextThread, token, target, intent, requestCode);
//    }

    @Override
    public Activity newActivity(Class<?> clazz, Context context,
                                IBinder token, Application application, Intent intent, ActivityInfo info,
                                CharSequence title, Activity parent, String id,
                                Object lastNonConfigurationInstance) throws InstantiationException,
            IllegalAccessException {

        Activity newActivity = super.newActivity(clazz, context, token, application, intent, info, title, parent,
                id, lastNonConfigurationInstance);

//        ReflectAccelerator.updateResource(Small.getContext());

        return newActivity;
    }

//    @Override
//    public Activity newActivity(ClassLoader cl, String className,
//                                Intent intent)
//            throws InstantiationException, IllegalAccessException,
//            ClassNotFoundException {
//        final String[] targetClassName = {className};
//        if (Build.VERSION.SDK_INT >= 28) {
//            ApkPluginLauncher.ActivityThreadHandlerCallback.tryReplaceActivityInfo(
//            intent, new ApkPluginLauncher.ActivityThreadHandlerCallback.ActivityInfoReplacer() {
//                @Override
//                public void replace(ActivityInfo info) {
//                    if (info == null) {
//                        return;
//                    }
//
//                    targetClassName[0] = info.targetActivity; // Redirect to the plugin activity
//                }
//            });
//        }
//
//        if (firstActivityCheckCount < FIRSTACTIVITY_CHECK_COUNT_MAX) {
////                if (sFirstActivityMonitor != null && !sFirstActivityMonitor.checkIsFirstActivityValid(className)) {
////                    // 让app自己重启, 先return null
////                    return null;
////                }
//            firstActivityCheckCount += 1;
//        }
//
//        Activity newActivity;
//
//        String activityName = Build.VERSION.SDK_INT >= 28 ? targetClassName[0] : className;
//        if (isPresetActivity(activityName)) {
//            Logging.error(TAG, activityName + " isPresetActivity return null");
//            return null;
//        }
//        try {
//
//            if (Build.VERSION.SDK_INT >= 28) {
//                newActivity = mHostInstrumentation.newActivity(cl, targetClassName[0], intent);
//            } else {
//                newActivity = (Activity) cl.loadClass(className).newInstance();
//            }
//        } catch (ClassNotFoundException exception) {
//            //加载默认activity
//            if (null == sNewActivityMonitor) {
//                throw exception;
//            }
//            String defaultActivityClass = sNewActivityMonitor.getDefaultActivityClass(activityName);
//            if (TextUtils.isEmpty(defaultActivityClass)) {
//                throw exception;
//            }
//            if (Build.VERSION.SDK_INT >= 28) {
//                newActivity = mHostInstrumentation.newActivity(cl, defaultActivityClass, intent);
//            } else {
//                newActivity = (Activity) cl.loadClass(defaultActivityClass).newInstance();
//            }
//        }
//
//
//        ReflectAccelerator.updateResource(Small.getContext());
//
//        return newActivity;
//    }


    /**
     * 判断是否预置activity
     *
     * @param activityName
     * @return
     */
    private static boolean isPresetActivity(String activityName) {
        String pattern = "com.yy.android.small.A[0-9]{0,2}";
        if (activityName.matches(pattern)) {
            return true;
        }
        return false;
    }

    @Override
    /** Prepare resources for REAL */
    public void callActivityOnCreate(Activity activity, android.os.Bundle icicle) {
        Log.i(TAG, "callActivityOnCreate");
        mHostInstrumentation.callActivityOnCreate(activity, icicle);
        ActivityInfo activityInfo = PluginController.getActivityInfo(activity.getClass().getName());
        if (activityInfo != null) {
            Log.i(TAG, "find out activityInfo , name is " + activityInfo.name);
            if (Build.VERSION.SDK_INT >= 28) {
                ReflectAccelerator.resetResourcesAndTheme(activity, activityInfo.getThemeResource());
            }

            Window window = activity.getWindow();
            window.setSoftInputMode(activityInfo.softInputMode);
            activity.setRequestedOrientation(activityInfo.screenOrientation);
        }

//        ActivityTaskMgr.INSTANCE.push2ActivityStack(activity);
//
//        do {
//            if (sLoadedActivities == null) break;
//            ActivityInfo ai = sLoadedActivities.get(activity.getClass().getName());
//            if (ai == null) break;
//
//            //TODO:是否需要判断在host中定义
//
//            applyActivityInfo(activity, ai);
//        } while (false);
//
//
//        ReflectAccelerator.updateResource(Small.getContext());
//
//        try {
//            mHostInstrumentation.callActivityOnCreate(activity, icicle);
//        } catch (Exception e) {
//            e.printStackTrace();
//            if ((e.toString().contains("android.content.res.Resources")
//            || e.toString().contains("Error inflating class")
//                    || e.toString().contains("java.lang.ArrayIndexOutOfBoundsException"))
//                    && !e.toString().contains("OutOfMemoryError")) {
//
//                if (activity != null) {
//                    String name = activity.getClass().getName();
//                    if (!sErrorActivityRecords.containsKey(name)) {
//                        sErrorActivityRecords.put(name, 0);
//                    }
//                    Log.e("ApkPluginLauncher", "callActivityOnCreate failed - " + name);
//                    Integer count = sErrorActivityRecords.get(name);
//                    Log.e("ApkPluginLauncher", "callActivityOnCreate failed - " + name + "count = " + count);
//                    if (count > 2) {
//                        StatisticsUtils.startActivityFailed("callActivityOnCreate2", e.toString());
//                        Log.e("ApkPluginLauncher", "callActivityOnCreate failed, throw exception");
//                        throw e;
//                    } else {
//                        sErrorActivityRecords.put(name, count + 1);
//                    }
//
//                    try {
//                        activity.finish();
//                    } catch (Throwable e1) {
//                        e1.printStackTrace();
//                    }
//                }
//
//                StatisticsUtils.startActivityFailed("callActivityOnCreate", e.toString());
//            } else {
//                throw e;
//            }
//        }
//
//        // Reset activity instrumentation if it was modified by some other applications #245
//        if (sBundleInstrumentation != null) {
//            try {
//                Object instrumentation = ReflectAccelerator.sActivity_mInstrumentation.get(activity);
//                if (instrumentation != sBundleInstrumentation) {
//                    ReflectAccelerator.sActivity_mInstrumentation.set(activity, sBundleInstrumentation);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
    }

    @Override
    public void callActivityOnStop(Activity activity) {
        mHostInstrumentation.callActivityOnStop(activity);
    }

//    @Override
//    public void callActivityOnDestroy(Activity activity) {
//
//        ActivityTaskMgr.INSTANCE.popFromActivityStack(activity);
//
//        do {
//            if (sLoadedActivities == null) break;
//            String realClazz = activity.getClass().getName();
//            ActivityInfo ai = sLoadedActivities.get(realClazz);
//            if (ai == null) break;
//
//            if (!ActivityLauncher.containsActivity(realClazz)) {
//                inqueueStubActivity(getLaunchMode(ai, activity.getIntent()), ai, realClazz);
//            }
//        } while (false);
//        mHostInstrumentation.callActivityOnDestroy(activity);
//    }

    @Override
    public void callActivityOnResume(Activity activity) {
        mHostInstrumentation.callActivityOnResume(activity);
    }

    @TargetApi(18)
    public UiAutomation getUiAutomation() {
        return mHostInstrumentation.getUiAutomation();
    }

    public void onCreate(Bundle bundle) {
        mHostInstrumentation.onCreate(bundle);
    }

    public void start() {
        mHostInstrumentation.start();
    }

    public void onStart() {
        mHostInstrumentation.onStart();
    }

    public boolean onException(Object obj, Throwable th) {
        return mHostInstrumentation.onException(obj, th);
    }

    public void sendStatus(int i, Bundle bundle) {
        mHostInstrumentation.sendStatus(i, bundle);
    }

    public void finish(int i, Bundle bundle) {
        mHostInstrumentation.finish(i, bundle);
    }

    public void setAutomaticPerformanceSnapshots() {
        mHostInstrumentation.setAutomaticPerformanceSnapshots();
    }

    public void startPerformanceSnapshot() {
        mHostInstrumentation.startPerformanceSnapshot();
    }

    public void endPerformanceSnapshot() {
        mHostInstrumentation.endPerformanceSnapshot();
    }

    public void onDestroy() {
        mHostInstrumentation.onDestroy();
    }

    public Context getContext() {
        return mHostInstrumentation.getContext();
    }

    public ComponentName getComponentName() {
        return mHostInstrumentation.getComponentName();
    }

    public Context getTargetContext() {
        return mHostInstrumentation.getTargetContext();
    }

    public boolean isProfiling() {
        return mHostInstrumentation.isProfiling();
    }

    public void startProfiling() {
        mHostInstrumentation.startProfiling();
    }

    public void stopProfiling() {
        mHostInstrumentation.stopProfiling();
    }

    public void setInTouchMode(boolean z) {
        mHostInstrumentation.setInTouchMode(z);
    }

    public void waitForIdle(Runnable runnable) {
        mHostInstrumentation.waitForIdle(runnable);
    }

    public void waitForIdleSync() {
        mHostInstrumentation.waitForIdleSync();
    }

    public void runOnMainSync(Runnable runnable) {
        mHostInstrumentation.runOnMainSync(runnable);
    }

    public Activity startActivitySync(Intent intent) {
        return mHostInstrumentation.startActivitySync(intent);
    }

    public void addMonitor(ActivityMonitor activityMonitor) {
        mHostInstrumentation.addMonitor(activityMonitor);
    }

    public ActivityMonitor addMonitor(IntentFilter intentFilter, ActivityResult activityResult, boolean z) {
        return mHostInstrumentation.addMonitor(intentFilter, activityResult, z);
    }

    public ActivityMonitor addMonitor(String str, ActivityResult activityResult, boolean z) {
        return mHostInstrumentation.addMonitor(str, activityResult, z);
    }

    public boolean checkMonitorHit(ActivityMonitor activityMonitor, int i) {
        return mHostInstrumentation.checkMonitorHit(activityMonitor, i);
    }

    public Activity waitForMonitor(ActivityMonitor activityMonitor) {
        return mHostInstrumentation.waitForMonitor(activityMonitor);
    }

    public Activity waitForMonitorWithTimeout(ActivityMonitor activityMonitor, long j) {
        return mHostInstrumentation.waitForMonitorWithTimeout(activityMonitor, j);
    }

    public void removeMonitor(ActivityMonitor activityMonitor) {
        mHostInstrumentation.removeMonitor(activityMonitor);
    }

    public boolean invokeMenuActionSync(Activity activity, int i, int i2) {
        return mHostInstrumentation.invokeMenuActionSync(activity, i, i2);
    }

    public boolean invokeContextMenuAction(Activity activity, int i, int i2) {
        return mHostInstrumentation.invokeContextMenuAction(activity, i, i2);
    }

    public void sendStringSync(String str) {
        mHostInstrumentation.sendStringSync(str);
    }

    public void sendKeySync(KeyEvent keyEvent) {
        mHostInstrumentation.sendKeySync(keyEvent);
    }

    public void sendKeyDownUpSync(int i) {
        mHostInstrumentation.sendKeyDownUpSync(i);
    }

    public void sendCharacterSync(int i) {
        mHostInstrumentation.sendCharacterSync(i);
    }

    public void sendPointerSync(MotionEvent motionEvent) {
        mHostInstrumentation.sendPointerSync(motionEvent);
    }

    public void sendTrackballEventSync(MotionEvent motionEvent) {
        mHostInstrumentation.sendTrackballEventSync(motionEvent);
    }

    public Application newApplication(ClassLoader classLoader, String str, Context context) throws
            InstantiationException, IllegalAccessException, ClassNotFoundException {
        return mHostInstrumentation.newApplication(classLoader, str, context);
    }

    public void callApplicationOnCreate(Application application) {
        mHostInstrumentation.callApplicationOnCreate(application);
    }


    public void callActivityOnRestoreInstanceState(Activity activity, Bundle bundle) {
        mHostInstrumentation.callActivityOnRestoreInstanceState(activity, bundle);
    }

    public void callActivityOnPostCreate(Activity activity, Bundle bundle) {
        mHostInstrumentation.callActivityOnPostCreate(activity, bundle);
    }

    public void callActivityOnNewIntent(Activity activity, Intent intent) {
        mHostInstrumentation.callActivityOnNewIntent(activity, intent);
    }

    public void callActivityOnStart(Activity activity) {
        mHostInstrumentation.callActivityOnStart(activity);
    }

    public void callActivityOnRestart(Activity activity) {
        mHostInstrumentation.callActivityOnRestart(activity);
    }

    public void callActivityOnSaveInstanceState(Activity activity, Bundle bundle) {
        mHostInstrumentation.callActivityOnSaveInstanceState(activity, bundle);
    }

    public void callActivityOnPause(Activity activity) {
        mHostInstrumentation.callActivityOnPause(activity);
    }

    public void callActivityOnUserLeaving(Activity activity) {
        mHostInstrumentation.callActivityOnUserLeaving(activity);
    }

    public void startAllocCounting() {
        mHostInstrumentation.startAllocCounting();
    }

    public void stopAllocCounting() {
        mHostInstrumentation.stopAllocCounting();
    }

    public Bundle getAllocCounts() {
        return mHostInstrumentation.getAllocCounts();
    }

    public Bundle getBinderCounts() {
        return mHostInstrumentation.getBinderCounts();
    }

//    private Intent wrapIntent(Intent intent) {
//        if (mFirstIntent != null && !(mFirstIntent.getComponent().equals(intent.getComponent()))) {
//            mFirstIntent.putExtra(Small.FIRST_ACTIVITY_INTENT_KEY, intent);
//            intent = mFirstIntent;
//            mFirstIntent = null;
//            return intent;
//        }
//        mFirstIntent = null;
//        ComponentName component = intent.getComponent();
//        String realClazz;
//        if (component == null) {
//            // Implicit way to start an activity
//            component = intent.resolveActivity(Small.getContext().getPackageManager());
//            if (component != null) return null; // ignore system or host action
//            realClazz = resolveActivity(intent);
//            if (realClazz == null) return null;
//        } else {
//            realClazz = component.getClassName();
//        }
//        if (sLoadedActivities == null) return null;
//
//        ActivityInfo ai = sLoadedActivities.get(realClazz);
//        if (ai == null) return null;
//
//        // Carry the real(plugin) class for incoming `newActivity' method.
//        intent.addCategory(REDIRECT_FLAG + realClazz);
//        String stubClazz = realClazz;
//        if (!ActivityLauncher.containsActivity(realClazz)) {
//            stubClazz = dequeueStubActivity(getLaunchMode(ai, intent), ai, realClazz);
//        }
//        Logging.warn(TAG, String.format("wrapIntent - %s ---> %s", realClazz, stubClazz));
//        intent.setComponent(new ComponentName(Small.getContext(), stubClazz));
//        return null;
//    }
//
//    private String resolveActivity(Intent intent) {
//        if (sLoadedIntentFilters == null) return null;
//
//        Iterator<Map.Entry<String, List<IntentFilter>>> it =
//                sLoadedIntentFilters.entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry<String, List<IntentFilter>> entry = it.next();
//            List<IntentFilter> filters = entry.getValue();
//            for (IntentFilter filter : filters) {
//                if (filter.hasAction(Intent.ACTION_VIEW)) {
//                }
//                if (filter.hasCategory(Intent.CATEGORY_DEFAULT)) {
//                    // custom action
//                    if (filter.hasAction(intent.getAction())) {
//                        // hit
//                        return entry.getKey();
//                    }
//                }
//            }
//        }
//        return null;
//    }
//
//    private String[] mStubQueue;
//
//    /**
//     * Get an usable stub activity clazz from real activity
//     */
//    private String dequeueStubActivity(int launchMode, ActivityInfo ai, String realActivityClazz) {
//        if (launchMode == ActivityInfo.LAUNCH_MULTIPLE) {
//            // In standard mode, the stub activity is reusable.
//            // Cause the `windowIsTranslucent' attribute cannot be dynamically set,
//            // We should choose the STUB activity with translucent or not here.
//            Resources.Theme theme = Small.getContext().getResources().newTheme();
//            theme.applyStyle(ai.getThemeResource(), true);
//            TypedArray sa = theme.obtainStyledAttributes(
//                    new int[]{android.R.attr.windowIsTranslucent});
//            boolean translucent = sa.getBoolean(0, false);
//            sa.recycle();
//            return translucent ? STUB_ACTIVITY_TRANSLUCENT : STUB_ACTIVITY_PREFIX;
//        }
//
//        int availableId = -1;
//        int stubId = -1;
//        int countForMode = STUB_ACTIVITIES_COUNT;
//        int countForAll = countForMode * 3; // 3=[singleTop, singleTask, singleInstance]
//        if (mStubQueue == null) {
//            // Lazy init
//            mStubQueue = new String[countForAll];
//        }
//        int offset = (launchMode - 1) * countForMode;
//        for (int i = 0; i < countForMode; i++) {
//            String usedActivityClazz = mStubQueue[i + offset];
//            if (usedActivityClazz == null) {
//                if (availableId == -1) availableId = i;
//            } else if (usedActivityClazz.equals(realActivityClazz)) {
//                stubId = i;
//            }
//        }
//        if (stubId != -1) {
//            availableId = stubId;
//        } else if (availableId != -1) {
//            mStubQueue[availableId + offset] = realActivityClazz;
//        } else {
//            // TODO:
//            Logging.error(TAG, "Launch mode " + launchMode + " is full");
//        }
//        return STUB_ACTIVITY_PREFIX + launchMode + availableId;
//    }
//
//    /**
//     * Unbind the stub activity from real activity
//     */
//    private void inqueueStubActivity(int launchMode, ActivityInfo ai, String realActivityClazz) {
//        if (launchMode == ActivityInfo.LAUNCH_MULTIPLE) return;
//        if (mStubQueue == null) return;
//
//        int countForMode = STUB_ACTIVITIES_COUNT;
//        int offset = (launchMode - 1) * countForMode;
//        for (int i = 0; i < countForMode; i++) {
//            String stubClazz = mStubQueue[i + offset];
//            if (stubClazz != null && stubClazz.equals(realActivityClazz)) {
//                mStubQueue[i + offset] = null;
//                break;
//            }
//        }
//    }

    private int getLaunchMode(ActivityInfo ai, Intent intent) {
        int launchMode = ai.launchMode;
        if (ai.launchMode == ActivityInfo.LAUNCH_MULTIPLE) {
            int flag = intent.getFlags();
            if ((flag & Intent.FLAG_ACTIVITY_SINGLE_TOP) == Intent.FLAG_ACTIVITY_SINGLE_TOP) {
                launchMode = ActivityInfo.LAUNCH_SINGLE_TOP;
            }
        }

        return launchMode;
    }
}
