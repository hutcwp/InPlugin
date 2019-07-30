package com.hutcwp.cow.util;

import android.content.pm.ActivityInfo;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by hutcwp on 2019-07-30 20:25
 * email: caiwenpeng@yy.com
 * YY: 909076244
 **/
public class PluginController {

    private static ConcurrentHashMap<String, ActivityInfo> sLoadedActivities = new ConcurrentHashMap<>();

    public static void addLoadActivity(ActivityInfo[] activities) {
        if (sLoadedActivities == null) sLoadedActivities = new ConcurrentHashMap<String, ActivityInfo>();
        for (ActivityInfo ai : activities) {
            sLoadedActivities.put(ai.name, ai);
        }
    }

    public static ActivityInfo getActivityInfo(String className) {
        if (sLoadedActivities == null) {
            return null;
        }

        return sLoadedActivities.get(className);
    }
}
