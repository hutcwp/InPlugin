package com.hutcwp.small.plugin;

import com.hutcwp.small.util.PluginUtil;

import java.io.File;

/**
 * Created by hutcwp on 2019-07-31 10:43
 * email: caiwenpeng@yy.com
 * YY: 909076244
 **/
public class Plugin {

    private File mApkFile;           //插件文件 xx.apk
    private PluginInfo pluginInfo;   //服务器下发插件信息
    private PluginRecord pluginRecord; //
    private boolean enable = false; //是否可用
    private String version;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Plugin(PluginInfo pluginInfo, PluginRecord pluginRecord) {
        this.pluginInfo = pluginInfo;
        this.pluginRecord = pluginRecord;
    }

    public PluginInfo getPluginInfo() {
        return pluginInfo;
    }

    public File getmApkFile() {
        if (mApkFile == null) {
            mApkFile = new File(PluginUtil.getPluginPath(pluginInfo.apkFileName));
        }

        return mApkFile;
    }

    public void setmApkFile(File mApkFile) {
        this.mApkFile = mApkFile;
    }

    public void setPluginInfo(PluginInfo pluginInfo) {
        this.pluginInfo = pluginInfo;
    }

    public PluginRecord getPluginRecord() {
        return pluginRecord;
    }

    public void setPluginRecord(PluginRecord pluginRecord) {
        this.pluginRecord = pluginRecord;
    }
}
