/*
 * Copyright 2015-present wequick.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.hutcwp.cow.luancher;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import com.hutcwp.cow.internal.InstrumentationWrapper;
import com.hutcwp.cow.util.ReflectAccelerator;


/**
 * This class launch the plugin activity by it's class name.
 *
 * <p>This class resolve the bundle who's <tt>pkg</tt> is specified as
 * <i>"*.app.*"</i> or <i>*.lib.*</i> in <tt>bundle.json</tt>.
 *
 * <ul>
 * <li>The <i>app</i> plugin contains some activities usually, while launching,
 * takes the bundle's <tt>uri</tt> as default activity. the other activities
 * can be specified by the bundle's <tt>rules</tt>.</li>
 *
 * <li>The <i>lib</i> plugin which can be included by <i>app</i> plugin
 * consists exclusively of global methods that operate on your product services.</li>
 * </ul>
 *
 * @see
 */
public class ApkPluginLauncher extends PluginLauncher {

    private static final String TAG = "ApkPluginLauncher";

    private static final int FIRSTACTIVITY_CHECK_COUNT_MAX = 4;
    private static int firstActivityCheckCount = 0;

    private static Instrumentation sHostInstrumentation;
    private static Instrumentation sBundleInstrumentation;


    @Override
    public void preSetUp(Application context) {
        super.preSetUp(context);
        if (sHostInstrumentation == null) {
            try {
                sHostInstrumentation = ReflectAccelerator.getHostInstrumentation();
                Instrumentation wrapper = new InstrumentationWrapper(sHostInstrumentation);
                ReflectAccelerator.setHostInstrumentation(wrapper);

                if (!sHostInstrumentation.getClass().getName().equals("android.app.Instrumentation")) {
                    sBundleInstrumentation = wrapper; // record for later replacement
                }

//                ReflectAccelerator.setActivityThreadHandlerCallback(new ActivityThreadHandlerCallback());
            } catch (Exception ignored) {
                ignored.printStackTrace();
                // Usually, cannot reach here
            }
        }
    }

    @Override
    public void setUp(Context context) {
        super.setUp(context);
    }

    @Override
    public void postSetUp() {
        super.postSetUp();
    }
}
