package com.hutcwp.small.plugin;

import android.content.Context;
import android.content.pm.PackageInfo;
import com.hutcwp.small.util.DLUtils;

import java.io.File;

public class PluginRecord {

    private static final String TAG = "PluginRecord";

    public PackageInfo packageInfo;
    String pluginPath;
    public PluginParser pluginParser;

    private PluginInfo pluginInfo;

    public PluginInfo getPluginInfo() {
        return pluginInfo;
    }

    public void setPluginInfo(PluginInfo pluginInfo) {
        this.pluginInfo = pluginInfo;
    }

    public PluginRecord() {
    }

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    public void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
    }

    public String getPluginPath() {
        return pluginPath;
    }

    public void setPluginPath(String pluginPath) {
        this.pluginPath = pluginPath;
    }

    public PluginParser getPluginParser() {
        return pluginParser;
    }

    public void setPluginParser(PluginParser pluginParser) {
        this.pluginParser = pluginParser;
    }


    /**
     * 生成PluginRecord
     * @param context
     * @param pluginInfo
     * @return
     */
    public static PluginRecord generatePluginRecord(Context context, PluginInfo pluginInfo) {
        File file = context.getFileStreamPath(pluginInfo.apkFileName);
        PluginRecord pluginRecord = new PluginRecord();
        pluginRecord.pluginPath = file.getAbsolutePath();
        pluginRecord.packageInfo = DLUtils.getPackageInfo(context, pluginRecord.pluginPath);
        pluginRecord.pluginParser = PluginParser.parsePackage(file);
        pluginRecord.setPluginInfo(pluginInfo);
        return pluginRecord;
    }

}