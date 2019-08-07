package com.hutcwp.small.internal;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.hutcwp.small.hook.AMSHookHelper;
import com.hutcwp.small.luancher.ApkPluginLauncher;
import com.hutcwp.small.util.RefInvoke;
import com.hutcwp.small.util.ReflectAccelerator;

import java.util.List;

/**
 * Created by hutcwp on 2019-08-01 16:32
 * email: caiwenpeng@yy.com
 * YY: 909076244
 **/
public class ActivityThreadHandlerCallback implements Handler.Callback {

    private static final String TAG = "ATHandlerCallback";
    private static final int LAUNCH_ACTIVITY = 100;
    private static final int ANDROID9_MSG = 159; //android9重构了代码，统一走159


    @Override
    public boolean handleMessage(Message msg) {
        Log.i(TAG, "handleMessage, msg.obj = " + msg.what);
        switch (msg.what) {
            // ActivityThread里面 "LAUNCH_ACTIVITY" 这个字段的值是100
            // 本来使用反射的方式获取最好, 这里为了简便直接使用硬编码
            case LAUNCH_ACTIVITY:
                handleLaunchActivity(msg);
                break;
            case ANDROID9_MSG:   //for API 28
                handleActivity(msg);
                break;
            default:
                break;
        }
        return false;
        /**
         *
         * 为什么返回false后不影响原逻辑，参见下面的类
         * android.os.Handler#dispatchMessage(android.os.Message)
         *
         * public void dispatchMessage(Message msg) {
         *    if (msg.callback != null) {
         *        handleCallback(msg);
         *   } else {
         *      if (mCallback != null) {
         *         if (mCallback.handleMessage(msg)) {
         *             return;
         *         }
         *     }
         *     handleMessage(msg);
         * }
         *}
         **/
    }

    private void handleLaunchActivity(Message msg) {
        // 这里简单起见,直接取出TargetActivity;
        Object obj = msg.obj;
        // 把替身恢复成真身
        Intent raw = (Intent) RefInvoke.getFieldObject(obj, "intent");
        Intent target = raw.getParcelableExtra(AMSHookHelper.EXTRA_TARGET_INTENT);
        Log.i(TAG, "raw Intent is " + raw.toString());
        if (target != null) {
            Log.e(TAG, "target Intent is " + target.toString());
            raw.setComponent(target.getComponent());
            try {
                Object/*ActivityClientRecord*/ r = msg.obj;
                ComponentName targetComponentName = target.getComponent();
                if (targetComponentName != null) {
                    try {
                        String className = targetComponentName.getClassName();
//                        ActivityInfo activityInfo = PluginManager.INSTANCE.getActivityInfoByQuery(className);
                        ActivityInfo activityInfo = ApkPluginLauncher.getsLoadedActivities().get(className);
                        if (activityInfo != null) {
                            Log.i(TAG, "find out activityInfo , name is " + activityInfo.name);
                            ReflectAccelerator.setActivityInfo(r, activityInfo);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "get activityInfo  error ", e);
            }
        }
    }

    /**
     * 处理Android28（9.0）
     *
     * @param msg
     */
    private void handleActivity(Message msg) {
        // 这里简单起见,直接取出TargetActivity;
        Object obj = msg.obj;
        List<Object> mActivityCallbacks = (List<Object>) RefInvoke.getFieldObject(obj, "mActivityCallbacks");
        if (mActivityCallbacks.size() > 0) {
            String className = "android.app.servertransaction.LaunchActivityItem";
            if (mActivityCallbacks.get(0).getClass().getCanonicalName().equals(className)) {
                Object object = mActivityCallbacks.get(0);

                Intent intent = (Intent) RefInvoke.getFieldObject(object, "mIntent");
                Intent target = intent.getParcelableExtra(AMSHookHelper.EXTRA_TARGET_INTENT);
                Log.e(TAG, "raw = " + intent.toString());
                Log.e(TAG, "target = " + target.toString());
                intent.setComponent(target.getComponent());
                // replace ai
                ActivityInfo activityInfo = (ActivityInfo) RefInvoke.getFieldObject(
                        object, "mInfo");
                Log.i(TAG, "default screenOrientation is " + activityInfo.screenOrientation);
                activityInfo.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

                ActivityInfo activityInfo2 = (ActivityInfo) RefInvoke.getFieldObject(object, "mInfo");
                Log.i(TAG, " after default screenOrientation is " + activityInfo2.screenOrientation);
//                RefInvoke.setFieldObject(object, "mInfo", activityInfo);
            }
        }
    }

//    static void tryReplaceActivityInfo(Intent intent, ActivityInfoReplacer replacer) {
//        if (intent == null) return;
//
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
//    }
}
