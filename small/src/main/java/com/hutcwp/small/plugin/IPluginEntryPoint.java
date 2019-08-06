package com.hutcwp.small.plugin;

/**
 * Created by hutcwp on 2019-08-06 11:15
 * email: caiwenpeng@yy.com
 * YY: 909076244
 **/
public interface IPluginEntryPoint {
    String ENTRY_POINT_ENUM_CLASS_NAME = "PluginEntryPoint";
    String ENUM_INSTANCE_NAME = "INSTANCE";

    void initialize();
    void mainEntry();
}
