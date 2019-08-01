package com.hutcwp.cow.plugin;

import java.io.File;

/**
 * Created by hutcwp on 2019-07-31 10:43
 * email: caiwenpeng@yy.com
 * YY: 909076244
 **/
public class Plugin {
    private String      mId;                // 插件ID
    private File mApkFile;           // 插件文件 xx.apk
    private String      mPackageName;       // 组件package name


    public String getmPackageName() {
        return mPackageName;
    }

    public void setmPackageName(String mPackageName) {
        this.mPackageName = mPackageName;
    }

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public File getmApkFile() {
        return mApkFile;
    }

    public void setmApkFile(File mApkFile) {
        this.mApkFile = mApkFile;
    }

}
