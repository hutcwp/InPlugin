package com.hutcwp.cow.plugin;

/**
 * Created by hutcwp on 2019-07-31 10:59
 * email: caiwenpeng@yy.com
 * YY: 909076244
 **/
public class PluginInfo {
    public static final int COM_TYPE_SMALL = 0;
    public static final int COM_TYPE_RN = 1;

    public String id;
    public String version;
    public String launchMode;
    public int loadMode;
    public String packageName;
    public int loadPriority;
    public int comType;
    public int downloadMode;
    public String apkFileName;
//    public Map<String, PatchInfo> patchList = new HashMap<>();
}
