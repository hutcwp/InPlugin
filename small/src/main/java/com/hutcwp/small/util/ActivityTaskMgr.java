package com.hutcwp.small.util;

import android.app.Activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by DEH on 2017/5/11.
 */

public enum ActivityTaskMgr {
    INSTANCE;

    private List<WeakReference<Activity>> activityList = new ArrayList<>();

    private ActivityTaskMgr() {
    }

    public void push2ActivityStack(Activity activity) {
        activityList.add(new WeakReference<>(activity));
    }

    public void popFromActivityStack(Activity activity) {
        for (int x = 0; x < activityList.size(); x++) {
            WeakReference<Activity> ref = activityList.get(x);
            if (ref != null && ref.get() != null && ref.get() == activity) {
                activityList.remove(ref);
            }
        }
    }

    public List<WeakReference<Activity>> getActivityList() {
        return activityList;
    }
}
