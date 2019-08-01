package com.hutcwp.small.internal;

import android.content.Intent;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by hutcwp on 2019-07-30 17:38
 * email: caiwenpeng@yy.com
 * YY: 909076244
 **/
public class HookHelper {

    private static final char REDIRECT_FLAG = '>';

    public static String unwrapIntent(Intent intent) {
        Set<String> categories = intent.getCategories();
        if (categories == null) return null;

        // Get plugin activity class name from categories
        Iterator<String> it = categories.iterator();
        while (it.hasNext()) {
            String category = it.next();
            if (category.charAt(0) == REDIRECT_FLAG) {
                return category.substring(1);
            }
        }
        return null;
    }

}
