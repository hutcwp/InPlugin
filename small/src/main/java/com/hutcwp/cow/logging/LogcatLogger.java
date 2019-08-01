package com.hutcwp.cow.logging;

import android.util.Log;

/**
 * User: lxl
 * Date: 10/18/16
 * Time: 3:03 PM
 */

public class LogcatLogger implements Logging.Logger {
    @Override
    public void verbose(String tag, String format, Object... args) {
        Log.v(tag, String.format(format, args));
    }

    @Override
    public void debug(String tag, String format, Object... args) {
        Log.d(tag, String.format(format, args));
    }

    @Override
    public void info(String tag, String format, Object... args) {
        Log.i(tag, String.format(format, args));
    }

    @Override
    public void warn(String tag, String format, Object... args) {
        Log.w(tag, String.format(format, args));
    }

    @Override
    public void error(String tag, String format, Object... args) {
        Log.e(tag, String.format(format, args));
    }

    @Override
    public void error(String tag, String format, Throwable throwable, Object... args) {
        Log.e(tag, String.format(format, args), throwable);
    }
}
