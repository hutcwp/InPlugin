package com.hutcwp.small.logging;

/**
 * User: lxl
 * Date: 10/18/16
 * Time: 2:56 PM
 */

public class Logging {

    private static Logger sLogger = new LogcatLogger(); // LogcatLogger by default

    public interface Logger {
        void verbose(String tag, String format, Object... args);

        void debug(String tag, String format, Object... args);

        void info(String tag, String format, Object... args);

        void warn(String tag, String format, Object... args);

        void error(String tag, String format, Object... args);

        void error(String tag, String format, Throwable throwable, Object... args);
    }

    public static void setLogger(Logger logger) {
        if (logger != null) {
            sLogger = logger;
        }
    }

    public static void verbose(String tag, String format, Object... args) {
        if (sLogger != null) {
            sLogger.verbose(tag, format, args);
        }
    }

    public static void debug(String tag, String format, Object... args) {
        if (sLogger != null) {
            sLogger.debug(tag, format, args);
        }
    }

    public static void info(String tag, String format, Object... args) {
        if (sLogger != null) {
            sLogger.info(tag, format, args);
        }
    }

    public static void warn(String tag, String format, Object... args) {
        if (sLogger != null) {
            sLogger.warn(tag, format, args);
        }
    }

    public static void error(String tag, String format, Object... args) {
        if (sLogger != null) {
            sLogger.error(tag, format, args);
        }
    }

    public static void error(String tag, String format, Throwable throwable, Object... args) {
        if (sLogger != null) {
            sLogger.error(tag, format, throwable, args);
        }
    }
}
