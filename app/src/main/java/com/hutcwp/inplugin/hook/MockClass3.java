package com.hutcwp.inplugin.hook;

import android.content.pm.PackageInfo;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author weishu
 * @date 16/1/7
 */
public class MockClass3 implements InvocationHandler {

    private Object mBase;

    public MockClass3(Object base) {
        mBase = base;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("getPackageInfo")) {
            return new PackageInfo();
        }
        return method.invoke(mBase, args);
    }
}
