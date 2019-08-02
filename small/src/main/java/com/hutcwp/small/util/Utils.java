package com.hutcwp.small.util;

import android.content.Context;
import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author weishu
 * @date 16/3/29
 */
public class Utils {

    /**
     * 把Assets里面得文件复制到 /data/data/files 目录下
     *
     * @param context
     * @param sourceName
     */
    public static void extractAssets(Context context, String sourceName) {
//        AssetManager am = context.getAssets();
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            File file = new File(sourceName);
            is = new FileInputStream(file);
            File extractFile = context.getFileStreamPath(file.getName());
            fos = new FileOutputStream(extractFile);
            byte[] buffer = new byte[1024];
            int count = 0;
            while ((count = is.read(buffer)) > 0) {
                fos.write(buffer, 0, count);
            }
            fos.flush();
        } catch (IOException e) {
            Log.e("extractAssets", "extractAssets error.", e);
            e.printStackTrace();
        } finally {
            closeSilently(is);
            closeSilently(fos);
        }
    }

    /**
     * 把插件的apk文件复制到 /data/data/files 目录下
     *
     * @param context
     * @param apkFile
     */
    public static void extractApkFile(Context context, File apkFile) {
//        AssetManager am = context.getAssets();
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            is = new FileInputStream(apkFile);
            File extractFile = context.getFileStreamPath(apkFile.getName());
            fos = new FileOutputStream(extractFile);
            byte[] buffer = new byte[1024];
            int count = 0;
            while ((count = is.read(buffer)) > 0) {
                fos.write(buffer, 0, count);
            }
            fos.flush();
        } catch (IOException e) {
            Log.e("extractAssets", "extractAssets error.", e);
            e.printStackTrace();
        } finally {
            closeSilently(is);
            closeSilently(fos);
        }
    }

    // --------------------------------------------------------------------------
    private static void closeSilently(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Throwable e) {
            // ignore
        }
    }
}
