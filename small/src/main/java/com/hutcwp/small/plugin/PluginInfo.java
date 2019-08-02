package com.hutcwp.small.plugin;

/**
 * Created by hutcwp on 2019-07-31 10:59
 * email: caiwenpeng@yy.com
 * YY: 909076244
 **/
public class PluginInfo {
    public String id;
    public String version;
    public int loadMode; // 0:启动加载/1:按需加载
    public String packageName;
    public int loadPriority;
    public int downloadMode;
    public String apkFileName;
}
