package com.hutcwp.plugina;

import android.content.res.AssetManager;

import java.lang.reflect.Method;

/**
 * Created by hutcwp on 2019-07-25 20:08
 * email: caiwenpeng@yy.com
 * YY: 909076244
 **/
public class Res {

    /**
     * 创建assetmanager
     *
     * @param dexPath
     * @return
     */
    public static AssetManager createAssetManager(String dexPath) {

        try {
            AssetManager assetManager = AssetManager.class.newInstance();
            Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
            addAssetPath.invoke(assetManager, dexPath);
            return assetManager;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


}
