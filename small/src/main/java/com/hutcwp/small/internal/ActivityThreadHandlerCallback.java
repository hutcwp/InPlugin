package com.hutcwp.small.internal;

/**
 * Created by hutcwp on 2019-07-30 17:36
 * email: caiwenpeng@yy.com
 * YY: 909076244
 **/

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;

/**
 * Class for restore activity info from Stub to Real
 */
public class ActivityThreadHandlerCallback implements Handler.Callback {

    private static final int LAUNCH_ACTIVITY = 100;

    interface ActivityInfoReplacer {
        void replace(ActivityInfo info);
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what != LAUNCH_ACTIVITY) return false;

        Object/*ActivityClientRecord*/ r = msg.obj;
//        Intent intent = ReflectAccelerator.getIntent(r);
//        String targetClass = HookHelper.unwrapIntent(intent);
//        if (targetClass == null) return false;

        //TODO:是否需要判断在host中是否定义

//        // Replace with the REAL activityInfo
//        if (sLoadedActivities == null) return false;
//
//        ActivityInfo targetInfo = sLoadedActivities.get(targetClass);

//        if (targetInfo == null) {
//            return false;
//        }
//
//        ReflectAccelerator.setActivityInfo(r, targetInfo);
        return false;
    }

    static void tryReplaceActivityInfo(Intent intent, ActivityInfoReplacer replacer) {
        if (intent == null) return;

//        String targetClass = unwrapIntent(intent);
//        boolean hasSetUp = Small.hasSetUp();
//        if (targetClass == null) {
//            // the activity was register in the host.
//            return;
//        }
//
//        if (!hasSetUp) {
//            // restarting an activity after application recreated,
//            // maybe upgrading or somehow the application was killed in background.
//            //    Small.setUp(null, false);
//        }
//
//        // replace with the real activityInfo
//        ActivityInfo targetInfo = sLoadedActivities.get(targetClass);
//        replacer.replace(targetInfo);
    }
}


