package com.hutcwp.small.plugin;

import com.hutcwp.small.util.PluginUtil;

import java.io.File;

/**
 * Created by hutcwp on 2019-07-31 10:43
 * email: caiwenpeng@yy.com
 * YY: 909076244
 **/
public class Plugin {

    private File mApkFile;           // 插件文件 xx.apk

    private PluginInfo pluginInfo; // 服务器下发插件信息

    public Plugin(PluginInfo pluginInfo) {
        this.pluginInfo = pluginInfo;
    }

    public PluginInfo getPluginInfo() {
        return pluginInfo;
    }

    public String getmPackageName() {
        return pluginInfo.packageName;
    }

    public String getmId() {
        return pluginInfo.id;
    }

    public File getmApkFile() {
        if (mApkFile == null) {
            mApkFile = new File(PluginUtil.getPluginPath(pluginInfo.apkFileName));
        }

        return mApkFile;
    }

}
