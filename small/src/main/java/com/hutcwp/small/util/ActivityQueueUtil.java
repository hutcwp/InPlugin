package com.hutcwp.small.util;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.Log;
import com.hutcwp.small.Small;
import com.hutcwp.small.hook.AMSHookHelper;
import com.hutcwp.small.luancher.ApkPluginLauncher;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by hutcwp on 2019-08-05 11:05
 * email: caiwenpeng@yy.com
 * YY: 909076244
 **/
public enum ActivityQueueUtil {

    INSTANCE;

    private static final String TAG = "ActivityQueueUtil";

    private static final int FIRSTACTIVITY_CHECK_COUNT_MAX = 4;
    private static int firstActivityCheckCount = 0;

    private static final String PACKAGE_NAME = Small.class.getPackage().getName();
    private static final String STUB_ACTIVITY_PREFIX = PACKAGE_NAME + ".A";
    private static final String STUB_ACTIVITY_TRANSLUCENT = STUB_ACTIVITY_PREFIX + '1';


    private static String[] mStubQueue;
    private static final int STUB_ACTIVITIES_COUNT = 4;

    private static final char REDIRECT_FLAG = '>';
    private static Intent mFirstIntent;
    public static final String FIRST_ACTIVITY_INTENT_KEY = "SmallFirstActivityIntentKey";


    /**
     * Get an usable stub activity clazz from real activity
     */
    public static String dequeueStubActivity(int launchMode, ActivityInfo ai, String realActivityClazz) {
        Log.i("test", "dequeueStubActivity: launchMode = " + launchMode);
        if (launchMode == ActivityInfo.LAUNCH_MULTIPLE) {
            // In standard mode, the stub activity is reusable.
            // Cause the `windowIsTranslucent' attribute cannot be dynamically set,
            // We should choose the STUB activity with translucent or not here.
            Resources.Theme theme = Small.getContext().getResources().newTheme();
            theme.applyStyle(ai.getThemeResource(), true);
            TypedArray sa = theme.obtainStyledAttributes(
                    new int[]{android.R.attr.windowIsTranslucent});
            boolean translucent = sa.getBoolean(0, false);
            sa.recycle();
            return translucent ? STUB_ACTIVITY_TRANSLUCENT : STUB_ACTIVITY_PREFIX;
        }

        int availableId = -1;
        int stubId = -1;
        int countForMode = STUB_ACTIVITIES_COUNT;
        int countForAll = countForMode * 3; // 3=[singleTop, singleTask, singleInstance]
        if (mStubQueue == null) {
            // Lazy init
            mStubQueue = new String[countForAll];
        }
        int offset = (launchMode - 1) * countForMode;
        for (int i = 0; i < countForMode; i++) {
            String usedActivityClazz = mStubQueue[i + offset];
            if (usedActivityClazz == null) {
                if (availableId == -1) availableId = i;
            } else if (usedActivityClazz.equals(realActivityClazz)) {
                stubId = i;
            }
        }
        if (stubId != -1) {
            availableId = stubId;
        } else if (availableId != -1) {
            mStubQueue[availableId + offset] = realActivityClazz;
        } else {
            // TODO:
            Log.e(TAG, "Launch mode " + launchMode + " is full");
        }
        return STUB_ACTIVITY_PREFIX + launchMode + availableId;
    }

    /**
     * Unbind the stub activity from real activity
     */
    public void inqueueStubActivity(int launchMode, ActivityInfo ai, String realActivityClazz) {
        if (launchMode == ActivityInfo.LAUNCH_MULTIPLE) return;
        if (mStubQueue == null) return;

        int countForMode = STUB_ACTIVITIES_COUNT;
        int offset = (launchMode - 1) * countForMode;
        for (int i = 0; i < countForMode; i++) {
            String stubClazz = mStubQueue[i + offset];
            if (stubClazz != null && stubClazz.equals(realActivityClazz)) {
                mStubQueue[i + offset] = null;
                break;
            }
        }
    }

    private int getLaunchMode(ActivityInfo ai, Intent intent) {
        int launchMode = ai.launchMode;
        Log.i("test", "ai = " + ai);
        Log.i("test", "launchMode = " + launchMode + " flag = " + intent.getFlags());
        if (ai.launchMode == ActivityInfo.LAUNCH_MULTIPLE) {
            int flag = intent.getFlags();
            if ((flag & Intent.FLAG_ACTIVITY_SINGLE_TOP) == Intent.FLAG_ACTIVITY_SINGLE_TOP) {
                launchMode = ActivityInfo.LAUNCH_SINGLE_TOP;
            }
        }

        return launchMode;
    }

    public Intent wrapIntent(Intent rawIntent) {
        Intent newIntent = new Intent();
        newIntent.putExtra(AMSHookHelper.EXTRA_TARGET_INTENT, rawIntent);

        ComponentName component = rawIntent.getComponent();
        String realClazz;
        if (component == null) {
            // Implicit way to start an activity
            component = rawIntent.resolveActivity(Small.getContext().getPackageManager());
            if (component != null) return null; // ignore system or host action
            realClazz = resolveActivity(rawIntent);
            if (realClazz == null) return null;
        } else {
            realClazz = component.getClassName();
        }
        if (ApkPluginLauncher.getsLoadedActivities() == null) return null;

        ActivityInfo ai = ApkPluginLauncher.getsLoadedActivities().get(realClazz);
        if (ai == null) return null;
        // Carry the real(plugin) class for incoming `newActivity' method.
        newIntent.addCategory(REDIRECT_FLAG + realClazz);
        String stubClazz = realClazz;
        if (ApkPluginLauncher.getsLoadedActivities() != null) {
            Log.d("test", "ApkPluginLauncher.getsLoadedActivities() = " + ApkPluginLauncher.getsLoadedActivities());
            if (!ApkPluginLauncher.containsActivity(stubClazz)) {
                stubClazz = dequeueStubActivity(getLaunchMode(ai, rawIntent), ai, realClazz);
                Log.i("test", "stubClazz is " + stubClazz);
            }
        }
        Log.i("test", String.format("wrapIntent - %s ---> %s", realClazz, stubClazz));
        newIntent.setComponent(new ComponentName(Small.getContext(), stubClazz));
        return newIntent;
    }

    private static String unwrapIntent(Intent intent) {
        Set<String> categories = intent.getCategories();
        if (categories == null) return null;

        // Get plugin activity class name from categories
        Iterator<String> it = categories.iterator();
        while (it.hasNext()) {
            String category = it.next();
            if (category.charAt(0) == REDIRECT_FLAG) {
                return category.substring(1);
            }
        }
        return null;
    }


    private String resolveActivity(Intent intent) {
        if (ApkPluginLauncher.getsLoadedIntentFilters() == null) return null;

        Iterator<Map.Entry<String, List<IntentFilter>>> it =
                ApkPluginLauncher.getsLoadedIntentFilters().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<IntentFilter>> entry = it.next();
            List<IntentFilter> filters = entry.getValue();
            for (IntentFilter filter : filters) {
                if (filter.hasAction(Intent.ACTION_VIEW)) {
                }
                if (filter.hasCategory(Intent.CATEGORY_DEFAULT)) {
                    // custom action
                    if (filter.hasAction(intent.getAction())) {
                        // hit
                        return entry.getKey();
                    }
                }
            }
        }
        return null;
    }

}
