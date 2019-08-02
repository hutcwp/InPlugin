package com.hutcwp.small.util;

import com.hutcwp.small.plugin.PluginInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hutcwp on 2019-07-31 10:57
 * email: caiwenpeng@yy.com
 * YY: 909076244
 **/
public class JsonUtil {

    public static final String pluginJsonStr = "{\n" +
            "\"pluginDir\":\"mPluginRecords\",\n" +
            "\"mPluginRecords\":[\n" +
            "{\n" +
            "\"id\":\"1\",\n" +
            "\"loadMode\":3,\n" +
            "\"loadPriority\":\"40\",\n" +
            "\"packageName\":\"com.yy.mobile.plugin.ycloud\",\n" +
            "\"apkFileName\":\"plugina.apk\",\n" +
            "\"version\":\"1.0.0\"\n" +
            "},\n" +
            "{\n" +
            "\"id\":\"11\",\n" +
            "\"loadMode\":2,\n" +
            "\"loadPriority\":\"600300\",\n" +
            "\"packageName\":\"com.yy.mobile.channelpk\",\n" +
            "\"apkFileName\":\"pluginb.apk\",\n" +
            "\"version\":\"1.0.0\"\n" +
            "}\n" +
            "],\n" +
            "\"version\":\"1.0.0\"\n" +
            "}";

    interface PluginKeys {
        String ID = "id";
        String PACKAGE_NAME = "packageName";
        String APKFILE_NAME = "apkFileName";
        String VERSION = "version";
        String LOAD_MODE = "loadMode";
        String URL = "url";
        String SHA1 = "sha1";
        String RULE_ID = "ruleId";
        String LOADPRIORITY = "loadPriority";
        String ENABLE = "enable";
        String FORCE = "force";
        String DOWNLOAD_MODE = "downloadMode";
        String PATCHS_INFO = "patchs";
    }

    interface ConfigKeys {
        String PLUGIN_DIR = "pluginDir";
        String VERSION = "version";
        String PLUGINS = "mPluginRecords";
    }


    public static List<PluginInfo> getPluginConfig(String configJson) {
        configJson = JsonUtil.pluginJsonStr;
        if (configJson != null && !configJson.isEmpty()) {
            try {
                List<PluginInfo> plugins = new ArrayList<>();
                JSONObject configObject = new JSONObject(configJson);
                JSONArray pluginArray = configObject.optJSONArray(ConfigKeys.PLUGINS);
                for (int i = 0; i < pluginArray.length(); ++i) {
                    JSONObject pluginObject = pluginArray.optJSONObject(i);
                    PluginInfo pluginInfo = new PluginInfo();
                    pluginInfo.id = pluginObject.optString(PluginKeys.ID);
                    pluginInfo.packageName = pluginObject.optString(PluginKeys.PACKAGE_NAME);
                    pluginInfo.apkFileName = pluginObject.optString(PluginKeys.APKFILE_NAME);
                    pluginInfo.version = pluginObject.optString(PluginKeys.VERSION);
                    pluginInfo.loadMode = pluginObject.optInt(PluginKeys.LOAD_MODE);
                    pluginInfo.loadPriority = pluginObject.optInt(PluginKeys.LOADPRIORITY);
                    pluginInfo.downloadMode = pluginObject.optInt(PluginKeys.DOWNLOAD_MODE, 0);
                    plugins.add(pluginInfo);
                }

                return plugins;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
