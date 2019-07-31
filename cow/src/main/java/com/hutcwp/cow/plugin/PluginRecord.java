package com.hutcwp.cow.plugin;

import android.content.pm.PackageInfo;

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
}