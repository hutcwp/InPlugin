package com.hutcwp.cow.hook;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.hutcwp.cow.util.PluginController;
import com.hutcwp.cow.util.RefInvoke;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author weishu
 * @date 16/1/7
 */
class HandlerCallbackHook implements Handler.Callback {


    private Handler mBase;

    public HandlerCallbackHook(Handler base) {
        mBase = base;
    }

    @Override
    public boolean handleMessage(Message msg) {
//        Log.i("test", "msg.what=" + msg.what);
        switch (msg.what) {
            // ActivityThread里面 "LAUNCH_ACTIVITY" 这个字段的值是100
            // 本来使用反射的方式获取最好, 这里为了简便直接使用硬编码
            case 100:
                handleLaunchActivity(msg);
                break;
            case 159:   //for API 28
                handleActivity(msg);
                break;
            default:
                break;
        }

        mBase.handleMessage(msg);
        return true;
    }

    private void handleLaunchActivity(Message msg) {
        // 这里简单起见,直接取出TargetActivity;
        Object obj = msg.obj;

        // 把替身恢复成真身
        Intent raw = (Intent) RefInvoke.getFieldObject(obj, "intent");
        Intent target = raw.getParcelableExtra(AMSHookHelper.EXTRA_TARGET_INTENT);
        Log.e("test", "raw = " + raw.toString());
        Log.e("test", "target = " + target.toString());
        raw.setComponent(target.getComponent());
        try {
            Object/*ActivityClientRecord*/ r = msg.obj;
            Class cls = r.getClass();
            String fieldName = "activityInfo";
            Field field = cls.getDeclaredField(fieldName);
            field.setAccessible(true);

            try {
                ActivityInfo activityInfo = PluginController.getActivityInfoByQuery(target.getComponent().getClassName());
                if (activityInfo != null) {
                    Log.e("test", "find out activityInfo , name is " + activityInfo.name);
                    field.set(r, activityInfo);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            Log.e("test", "get activity info  error ", e);
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
                Log.e("test", "raw = " + intent.toString());
                Log.e("test", "target = " + target.toString());
                intent.setComponent(target.getComponent());
                // replace ai
                ActivityInfo activityInfo = (ActivityInfo) RefInvoke.getFieldObject(
                        object, "mInfo");
                Log.i("test", "default screenOrientation is " + activityInfo.screenOrientation);
                activityInfo.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

                ActivityInfo activityInfo2 = (ActivityInfo) RefInvoke.getFieldObject(object, "mInfo");
                Log.i("test", " after default screenOrientation is " + activityInfo2.screenOrientation);
//                RefInvoke.setFieldObject(object, "mInfo", activityInfo);
            }
        }
    }
}
