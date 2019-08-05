package com.hutcwp.small.hook;

import android.content.Intent;
import android.util.Log;
import com.hutcwp.small.util.ActivityQueueUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

class ActivityManagerProxyHook implements InvocationHandler {

    private static final String TAG = "ActivityManagerProxyHook";

    Object mBase;

    public ActivityManagerProxyHook(Object base) {
        mBase = base;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Log.e("test", method.getName());

        if ("startActivity".equals(method.getName())) {
            // 只拦截这个方法
            // 替换参数, 任你所为;甚至替换原始Activity启动别的Activity偷梁换柱
            // 找到参数里面的第一个Intent 对象
            Intent raw;
            int index = 0;
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Intent) {
                    index = i;
                    break;
                }
            }

            raw = (Intent) args[index];
            Intent newIntent = ActivityQueueUtil.INSTANCE.wrapIntent(raw);
            // 替换掉Intent, 达到欺骗AMS的目的
            if (newIntent != null) {
                args[index] = newIntent;
            }
            Log.d("test", "hook success. newIntent = " + newIntent);
            return method.invoke(mBase, args);

        }
        return method.invoke(mBase, args);
    }

}