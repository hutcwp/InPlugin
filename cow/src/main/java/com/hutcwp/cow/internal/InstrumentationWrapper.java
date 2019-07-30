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
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * Class for redirect activity from Stub(AndroidManifest.xml) to Real(Plugin)
 */
public class InstrumentationWrapper extends Instrumentation {

    private static final String TAG = "InstrumentationWrapper";
    private static final int STUB_ACTIVITIES_COUNT = 4;
    private Instrumentation sHostInstrumentation;

    public InstrumentationWrapper(Instrumentation hostInstrumentation) {
        sHostInstrumentation = hostInstrumentation;
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
//        return ReflectAccelerator.execStartActivity(sHostInstrumentation,
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
//        return ReflectAccelerator.execStartActivity(sHostInstrumentation,
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
//            ApkPluginLauncher2.ActivityThreadHandlerCallback.tryReplaceActivityInfo(
//            intent, new ApkPluginLauncher2.ActivityThreadHandlerCallback.ActivityInfoReplacer() {
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
//                newActivity = sHostInstrumentation.newActivity(cl, targetClassName[0], intent);
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
//                newActivity = sHostInstrumentation.newActivity(cl, defaultActivityClass, intent);
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
//            sHostInstrumentation.callActivityOnCreate(activity, icicle);
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
//                    Log.e("ApkPluginLauncher2", "callActivityOnCreate failed - " + name);
//                    Integer count = sErrorActivityRecords.get(name);
//                    Log.e("ApkPluginLauncher2", "callActivityOnCreate failed - " + name + "count = " + count);
//                    if (count > 2) {
//                        StatisticsUtils.startActivityFailed("callActivityOnCreate2", e.toString());
//                        Log.e("ApkPluginLauncher2", "callActivityOnCreate failed, throw exception");
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
        sHostInstrumentation.callActivityOnStop(activity);
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
//        sHostInstrumentation.callActivityOnDestroy(activity);
//    }

    @Override
    public void callActivityOnResume(Activity activity) {
        sHostInstrumentation.callActivityOnResume(activity);
    }

    @TargetApi(18)
    public UiAutomation getUiAutomation() {
        return sHostInstrumentation.getUiAutomation();
    }

    public void onCreate(Bundle bundle) {
        sHostInstrumentation.onCreate(bundle);
    }

    public void start() {
        sHostInstrumentation.start();
    }

    public void onStart() {
        sHostInstrumentation.onStart();
    }

    public boolean onException(Object obj, Throwable th) {
        return sHostInstrumentation.onException(obj, th);
    }

    public void sendStatus(int i, Bundle bundle) {
        sHostInstrumentation.sendStatus(i, bundle);
    }

    public void finish(int i, Bundle bundle) {
        sHostInstrumentation.finish(i, bundle);
    }

    public void setAutomaticPerformanceSnapshots() {
        sHostInstrumentation.setAutomaticPerformanceSnapshots();
    }

    public void startPerformanceSnapshot() {
        sHostInstrumentation.startPerformanceSnapshot();
    }

    public void endPerformanceSnapshot() {
        sHostInstrumentation.endPerformanceSnapshot();
    }

    public void onDestroy() {
        sHostInstrumentation.onDestroy();
    }

    public Context getContext() {
        return sHostInstrumentation.getContext();
    }

    public ComponentName getComponentName() {
        return sHostInstrumentation.getComponentName();
    }

    public Context getTargetContext() {
        return sHostInstrumentation.getTargetContext();
    }

    public boolean isProfiling() {
        return sHostInstrumentation.isProfiling();
    }

    public void startProfiling() {
        sHostInstrumentation.startProfiling();
    }

    public void stopProfiling() {
        sHostInstrumentation.stopProfiling();
    }

    public void setInTouchMode(boolean z) {
        sHostInstrumentation.setInTouchMode(z);
    }

    public void waitForIdle(Runnable runnable) {
        sHostInstrumentation.waitForIdle(runnable);
    }

    public void waitForIdleSync() {
        sHostInstrumentation.waitForIdleSync();
    }

    public void runOnMainSync(Runnable runnable) {
        sHostInstrumentation.runOnMainSync(runnable);
    }

    public Activity startActivitySync(Intent intent) {
        return sHostInstrumentation.startActivitySync(intent);
    }

    public void addMonitor(ActivityMonitor activityMonitor) {
        sHostInstrumentation.addMonitor(activityMonitor);
    }

    public ActivityMonitor addMonitor(IntentFilter intentFilter, ActivityResult activityResult, boolean z) {
        return sHostInstrumentation.addMonitor(intentFilter, activityResult, z);
    }

    public ActivityMonitor addMonitor(String str, ActivityResult activityResult, boolean z) {
        return sHostInstrumentation.addMonitor(str, activityResult, z);
    }

    public boolean checkMonitorHit(ActivityMonitor activityMonitor, int i) {
        return sHostInstrumentation.checkMonitorHit(activityMonitor, i);
    }

    public Activity waitForMonitor(ActivityMonitor activityMonitor) {
        return sHostInstrumentation.waitForMonitor(activityMonitor);
    }

    public Activity waitForMonitorWithTimeout(ActivityMonitor activityMonitor, long j) {
        return sHostInstrumentation.waitForMonitorWithTimeout(activityMonitor, j);
    }

    public void removeMonitor(ActivityMonitor activityMonitor) {
        sHostInstrumentation.removeMonitor(activityMonitor);
    }

    public boolean invokeMenuActionSync(Activity activity, int i, int i2) {
        return sHostInstrumentation.invokeMenuActionSync(activity, i, i2);
    }

    public boolean invokeContextMenuAction(Activity activity, int i, int i2) {
        return sHostInstrumentation.invokeContextMenuAction(activity, i, i2);
    }

    public void sendStringSync(String str) {
        sHostInstrumentation.sendStringSync(str);
    }

    public void sendKeySync(KeyEvent keyEvent) {
        sHostInstrumentation.sendKeySync(keyEvent);
    }

    public void sendKeyDownUpSync(int i) {
        sHostInstrumentation.sendKeyDownUpSync(i);
    }

    public void sendCharacterSync(int i) {
        sHostInstrumentation.sendCharacterSync(i);
    }

    public void sendPointerSync(MotionEvent motionEvent) {
        sHostInstrumentation.sendPointerSync(motionEvent);
    }

    public void sendTrackballEventSync(MotionEvent motionEvent) {
        sHostInstrumentation.sendTrackballEventSync(motionEvent);
    }

    public Application newApplication(ClassLoader classLoader, String str, Context context) throws
            InstantiationException, IllegalAccessException, ClassNotFoundException {
        return sHostInstrumentation.newApplication(classLoader, str, context);
    }

    public void callApplicationOnCreate(Application application) {
        sHostInstrumentation.callApplicationOnCreate(application);
    }


    public void callActivityOnRestoreInstanceState(Activity activity, Bundle bundle) {
        sHostInstrumentation.callActivityOnRestoreInstanceState(activity, bundle);
    }

    public void callActivityOnPostCreate(Activity activity, Bundle bundle) {
        sHostInstrumentation.callActivityOnPostCreate(activity, bundle);
    }

    public void callActivityOnNewIntent(Activity activity, Intent intent) {
        sHostInstrumentation.callActivityOnNewIntent(activity, intent);
    }

    public void callActivityOnStart(Activity activity) {
        sHostInstrumentation.callActivityOnStart(activity);
    }

    public void callActivityOnRestart(Activity activity) {
        sHostInstrumentation.callActivityOnRestart(activity);
    }

    public void callActivityOnSaveInstanceState(Activity activity, Bundle bundle) {
        sHostInstrumentation.callActivityOnSaveInstanceState(activity, bundle);
    }

    public void callActivityOnPause(Activity activity) {
        sHostInstrumentation.callActivityOnPause(activity);
    }

    public void callActivityOnUserLeaving(Activity activity) {
        sHostInstrumentation.callActivityOnUserLeaving(activity);
    }

    public void startAllocCounting() {
        sHostInstrumentation.startAllocCounting();
    }

    public void stopAllocCounting() {
        sHostInstrumentation.stopAllocCounting();
    }

    public Bundle getAllocCounts() {
        return sHostInstrumentation.getAllocCounts();
    }

    public Bundle getBinderCounts() {
        return sHostInstrumentation.getBinderCounts();
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
