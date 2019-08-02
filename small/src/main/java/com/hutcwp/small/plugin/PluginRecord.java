package com.hutcwp.small.plugin;

import android.content.Context;
import android.content.pm.PackageInfo;
import com.hutcwp.small.luancher.PluginLauncher;
import com.hutcwp.small.util.DLUtils;

import java.io.File;
import java.util.List;

public class PluginRecord {

    private static final String TAG = "PluginRecord";

    private String pluginPath;             // 插件地址
    private PluginInfo pluginInfo;         //插件信息
    private PackageInfo packageInfo;        //包信息
    private PluginParser pluginParser;      // 插件分析器
    private PluginLauncher pluginLauncher; //插件加载器

    private PluginRecord() {
    }

    public PluginInfo getPluginInfo() {
        return pluginInfo;
    }

    public void setPluginInfo(PluginInfo pluginInfo) {
        this.pluginInfo = pluginInfo;
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

    public PluginLauncher getPluginLauncher() {
        return pluginLauncher;
    }

    public void setPluginLauncher(PluginLauncher pluginLauncher) {
        this.pluginLauncher = pluginLauncher;
    }

    /**
     * 生成PluginRecord
     *
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

    public void launch() {
        if (pluginLauncher.preloadPlugin(this)) {
            pluginLauncher.loadPlugin(this);
        }

    }

    public void applyLaunchers(List<PluginLauncher> mPluginLaunchers) {
        for (PluginLauncher launcher : mPluginLaunchers) {
            if (launcher.resolvePlugin(this)) {
                setPluginLauncher(launcher);
                return;
            }
        }
    }

}