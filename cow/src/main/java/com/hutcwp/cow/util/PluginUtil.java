package com.hutcwp.cow.util;

import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by hutcwp on 2019-07-31 16:01
 * email: caiwenpeng@yy.com
 * YY: 909076244
 **/
public class PluginUtil {

    /**
     * 获取插件存放目录
     *
     * @return 插件目录
     */
    public static String getPluginDir() {
        boolean getFromExternal = true; // 是否从外存读取
        if (getFromExternal) {
            // 从存储卡/mlugins/路径下读取
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                return Environment.getExternalStorageDirectory().getAbsoluteFile() + File.separator + "mplugins";
            } else {
                Log.e("test", "无法读取外存。");
            }
        }
        return null;
    }

    /**
     * 获取插件详细路径
     *
     * @param pathDir
     * @param apkFileName
     * @return
     */
    public static String getPluginPath(String pathDir, String apkFileName) {
        return pathDir + File.separator + apkFileName;
    }

    public static String getPluginPath(String apkFileName) {
        return getPluginDir() + File.separator + apkFileName;
    }
}
