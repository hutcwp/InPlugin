package com.hutcwp.small.plugin;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.util.Log;
import com.hutcwp.small.Small;
import com.hutcwp.small.luancher.PluginLauncher;
import com.hutcwp.small.util.DLUtils;
import com.hutcwp.small.util.PluginUtil;
import com.hutcwp.small.util.Utils;

import java.io.File;
import java.util.List;

public class PluginRecord {

    private static final String TAG = "PluginRecord";

    private String pluginPath;              // 插件地址
    private PluginInfo pluginInfo;          //插件信息
    private PackageInfo packageInfo;        //包信息
    private PluginParser pluginParser;      // 插件分析器
    private PluginLauncher pluginLauncher;  //插件加载器

    private PluginRecord() {
    }

    public PluginInfo getPluginInfo() {
        return pluginInfo;
    }

    public void setPluginInfo(PluginInfo pluginInfo) {
        this.pluginInfo = pluginInfo;
    }

    public PackageInfo getPackageInfo() {
        if (packageInfo == null) {
            packageInfo = DLUtils.getPackageInfo(Small.getContext(), pluginPath);
        }
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
     * @param mPluginLaunchers
     * @return
     */
    public static PluginRecord generatePluginRecord(
            Context context, PluginInfo pluginInfo, List<PluginLauncher> mPluginLaunchers) {
        File file = context.getFileStreamPath(pluginInfo.apkFileName);
        PluginRecord pluginRecord = new PluginRecord();
        pluginRecord.pluginPath = file.getAbsolutePath();
        pluginRecord.packageInfo = DLUtils.getPackageInfo(context, pluginRecord.pluginPath);
        pluginRecord.pluginParser = PluginParser.parsePackage(file);
        pluginRecord.pluginInfo = pluginInfo;
        pluginRecord.applyLaunchers(mPluginLaunchers);
        return pluginRecord;
    }

    public void launch() {
        if (isExtensionPlugin()) {
            checkPlugin();
        }

        if (pluginLauncher.preloadPlugin(this)) {
            pluginLauncher.loadPlugin(this);
        }
    }

    private void checkPlugin() {
        if (Small.getContext().getFileStreamPath(pluginInfo.apkFileName).exists()) {
            Log.e(TAG, "file not exist. path is " + getPluginPath());
        }
    }

    private boolean isExtensionPlugin() {
        return this.pluginInfo.loadMode != 0;
    }

    /**
     * 激活插件
     *
     * @param
     */
    public boolean activePlugin() {
        final String className = getPackageInfo().packageName
                + "." + IPluginEntryPoint.ENTRY_POINT_ENUM_CLASS_NAME;

        try {
            final Class<java.lang.Enum> pluginClz =
                    (Class<java.lang.Enum>) Class.forName(className);

            final Object obj = Enum.valueOf(pluginClz,
                    IPluginEntryPoint.ENUM_INSTANCE_NAME);

            if (obj instanceof IPluginEntryPoint) {
                ((IPluginEntryPoint) obj).initialize();
            }
            return true;
        } catch (Throwable e) {
            Log.e("Plugin", "activePlugin initialize failed. msg = ", e);
        }
        return false;
    }

    /**
     * 启动组件
     */
    public boolean execPlugin() {
        final String className = getPackageInfo().packageName
                + "." + IPluginEntryPoint.ENTRY_POINT_ENUM_CLASS_NAME;

        try {
            final Class<java.lang.Enum> pluginClz =
                    (Class<java.lang.Enum>) Class.forName(className);

            final Object obj = Enum.valueOf(pluginClz,
                    IPluginEntryPoint.ENUM_INSTANCE_NAME);

            if (obj instanceof IPluginEntryPoint) {
                ((IPluginEntryPoint) obj).mainEntry();
            }
            return true;
        } catch (Throwable e) {
            Log.e("Plugin", "activePlugin mainEntry failed msg", e);
            return false;
        }
    }

    /**
     * copy apk 到内存
     */
    private void copyToAssert() {
        Utils.extractAssets(Small.getContext(), PluginUtil.getPluginPath(getPluginInfo().apkFileName));
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