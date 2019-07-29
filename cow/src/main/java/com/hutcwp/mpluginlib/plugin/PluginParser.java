package com.hutcwp.mpluginlib.plugin;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.os.PatternMatcher;
import android.util.AttributeSet;
import android.util.Log;
import com.hutcwp.mpluginlib.PluginManager;
import com.hutcwp.mpluginlib.ReflectAccelerator;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class consists exclusively of methods that operate on external bundle.
 * <p>
 * It's a lite edition of <a href="https://github.com/android/platform_frameworks_base/blob/gingerbread-release/core%2Fjava%2Fandroid%2Fcontent%2Fpm%2FPackageParser.java">PackageParser.java</a>
 * which focus on:
 * <ul>
 * <li>package.versionCode</li>
 * <li>package.versionName</li>
 * <li>package.theme</li>
 * <li>activities</li>
 * </ul>
 * <p>
 * Furthermore, this class will also collect the <b>&lt;intent-filter&gt;</b> information for each activity.
 */
public class PluginParser {

    private static final String TAG = "PluginParser";

    private static final String mLibDir = "lib/armeabi-v7a/";

    /* com.android.internal.R.styleable.* on
     * https://github.com/android/platform_frameworks_base/blob/gingerbread-release/core%2Fres%2Fres%2Fvalues%2Fpublic.xml
     */
    private static final class R {
        public static final class styleable {
            // manifest
            public static final int[] AndroidManifest = {0x0101021b, 0x0101021c};
            public static final int AndroidManifest_versionCode = 0;
            public static final int AndroidManifest_versionName = 1;
            // application
            public static int[] AndroidManifestApplication = {
                    0x01010000, 0x01010001, 0x01010003, 0x010102d3
            };
            public static int AndroidManifestApplication_theme = 0;
            public static int AndroidManifestApplication_label = 1; // for ABIs (Depreciated)
            public static int AndroidManifestApplication_name = 2;
            public static int AndroidManifestApplication_hardwareAccelerated = 3;
            // activity
            public static int[] AndroidManifestActivity = {
                    0x01010000, 0x01010001, 0x01010002, 0x01010003,
                    0x0101001d, 0x0101001e, 0x0101001f, 0x0101022b,
                    0x010102d3,
            };
            public static int AndroidManifestActivity_theme = 0;
            public static int AndroidManifestActivity_label = 1;
            public static int AndroidManifestActivity_icon = 2;
            public static int AndroidManifestActivity_name = 3;
            public static int AndroidManifestActivity_launchMode = 4;
            public static int AndroidManifestActivity_screenOrientation = 5;
            public static int AndroidManifestActivity_configChanges = 6;
            public static int AndroidManifestActivity_windowSoftInputMode = 7;
            public static int AndroidManifestActivity_hardwareAccelerated = 8;


            // data (for intent-filter)
            public static int[] AndroidManifestData = {
                    0x01010026, 0x01010027, 0x01010028, 0x01010029,
                    0x0101002a, 0x0101002b, 0x0101002c
            };
            public static int AndroidManifestData_mimeType = 0;
            public static int AndroidManifestData_scheme = 1;
            public static int AndroidManifestData_host = 2;
            public static int AndroidManifestData_port = 3;
            public static int AndroidManifestData_path = 4;
            public static int AndroidManifestData_pathPrefix = 5;
            public static int AndroidManifestData_pathPattern = 6;
        }
    }

    private String mPluginFile;
    private WeakReference<byte[]> mReadBuffer;
    private PackageInfo mPackageInfo;
    private XmlResourceParser parser;
    private Resources res;
    private ConcurrentHashMap<String, List<IntentFilter>> mIntentFilters;
    private boolean mNonResources;
    private boolean mUsesHardwareAccelerated;

    private String mLauncherActivityName;

    private String mBuildVersion = "";
    private String mBuildDate = "";
    private HashMap<String, String> mDependedInfo = null;

    private Context mContext;

    public PluginParser(File pluginFile) {
        mPluginFile = pluginFile.getPath();
        mContext = PluginManager.mBaseContext;
    }

    public static PluginParser parsePackage(File pluginFile) {
        if (pluginFile == null) {
            Log.e(TAG, "parsePackage bug pluginFile is null");
            return null;
        }

        if (!pluginFile.exists()) {
            Log.e(TAG, "parsePackage bug pluginFile[%s] is not exist" + pluginFile.getAbsoluteFile());
            return null;
        }

        PluginParser bp = new PluginParser(pluginFile);
        if (!bp.parsePackage()) {
            return null;
        }
        bp.collectActivities();

        return bp;
    }

    public String getPackageName() {
        return mPackageInfo.packageName;
    }

    public boolean parsePackage() {
        AssetManager assmgr = null;
        boolean assetError = true;
        try {
            assmgr = ReflectAccelerator.newAssetManager();
            if (assmgr == null) return false;

            int cookie = ReflectAccelerator.addAssetPath(assmgr, mPluginFile);
            if (cookie != 0) {
                parser = assmgr.openXmlResourceParser(cookie, "AndroidManifest.xml");
                assetError = false;
            } else {
                Log.e(TAG, "Failed adding asset path:" + mPluginFile);
            }
        } catch (Exception e) {
            Log.e(TAG, "Unable to read AndroidManifest.xml of "
                    + mPluginFile, e);
        }
        if (assetError) {
            if (assmgr != null) assmgr.close();
            return false;
        }

        res = new Resources(assmgr, mContext.getResources().getDisplayMetrics(), null);

        return parsePackage(res, parser);
    }

    private boolean parsePackage(Resources res, XmlResourceParser parser) {
        AttributeSet attrs = parser;
        mPackageInfo = new PackageInfo();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(res.getAssets().open("build_version.txt")));

            // do reading, usually loop until end of file reading
            String line;
            while ((line = reader.readLine()) != null) {
                String[] splitResult = line.split(":");
                if (splitResult.length == 2) {
                    if (splitResult[0].equals("self")) {
                        mBuildVersion = splitResult[1];
                    } else if (splitResult[0].equals("buildDate")) {
                        mBuildDate = splitResult[1];
                    } else {
                        if (mDependedInfo == null) mDependedInfo = new HashMap<>();
                        mDependedInfo.put(splitResult[0], splitResult[1]);
                    }
                }
                //process line
            }
        } catch (IOException e) {
            //log the exception
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }

        try {
            int type;
            while ((type = parser.next()) != XmlResourceParser.START_TAG
                    && type != XmlResourceParser.END_DOCUMENT) ;


            // <manifest ...
            mPackageInfo.packageName = parser.getAttributeValue(null, "package").intern();

            Log.d(TAG, String.format("mPackageName: %s, mPackageInfo.packageName: %s",
                    getPackageName(), mPackageInfo.packageName));

            // After gradle-small 0.9.0, we roll out
            // `The Small exclusive flags`
            //  F    F    F    F    F    F    F    F
            // 1111 1111 1111 1111 1111 1111 1111 1111
            // ^^^^ ^^^^ ^^^^ ^^^^ ^^^^
            //       ABI Flags (20)
            //                          ^
            //                 nonResources Flag (1)
            //                           ^^^ ^^^^ ^^^^
            //                     platformBuildVersionCode (11) => MAX=0x7FF=4095
            int flags = parser.getAttributeIntValue(null, "platformBuildVersionCode", 0);
            mNonResources = (flags & 0x800) != 0;

            TypedArray sa = res.obtainAttributes(attrs,
                    R.styleable.AndroidManifest);
            mPackageInfo.versionCode = sa.getInteger(
                    R.styleable.AndroidManifest_versionCode, 0);
            String versionName = sa.getString(
                    R.styleable.AndroidManifest_versionName);
            if (versionName != null) {
                mPackageInfo.versionName = versionName.intern();
            }

            // <application ...
            while ((type = parser.next()) != XmlResourceParser.END_DOCUMENT) {
                if (type == XmlResourceParser.TEXT) {
                    continue;
                }

                String tagName = parser.getName();
                if (tagName.equals("application")) {
                    ApplicationInfo host = mContext.getApplicationInfo();
                    ApplicationInfo app = new ApplicationInfo(host);

                    sa = res.obtainAttributes(attrs,
                            R.styleable.AndroidManifestApplication);

                    String name = sa.getString(
                            R.styleable.AndroidManifestApplication_name);
                    if (name != null) {
                        app.className = name.intern();
                    } else {
                        app.className = null;
                    }

                    app.theme = sa.getResourceId(
                            R.styleable.AndroidManifestApplication_theme, 0);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        mUsesHardwareAccelerated = sa.getBoolean(
                                R.styleable.AndroidManifestApplication_hardwareAccelerated,
                                host.targetSdkVersion >= Build.VERSION_CODES.ICE_CREAM_SANDWICH);
                    }

                    mPackageInfo.applicationInfo = app;
                    break;
                }
            }

            sa.recycle();
            return true;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean collectActivities() {
        if (mPackageInfo == null || mPackageInfo.applicationInfo == null) return false;
        AttributeSet attrs = parser;

        int type;
        try {
            List<ActivityInfo> activities = new ArrayList<ActivityInfo>();
            while ((type = parser.next()) != XmlResourceParser.END_DOCUMENT) {
                if (type != XmlResourceParser.START_TAG) {
                    continue;
                }

                String tagName = parser.getName();
                if (!tagName.equals("activity")) continue;

                // <activity ...
                ActivityInfo ai = new ActivityInfo();
                ai.applicationInfo = mPackageInfo.applicationInfo;
                ai.packageName = ai.applicationInfo.packageName;

                TypedArray sa = res.obtainAttributes(attrs,
                        R.styleable.AndroidManifestActivity);
                String name = sa.getString(R.styleable.AndroidManifestActivity_name);
                if (name != null) {
                    ai.name = ai.targetActivity = buildClassName(getPackageName(), name);
                }
                ai.labelRes = sa.getResourceId(R.styleable.AndroidManifestActivity_label, 0);
                ai.icon = sa.getResourceId(R.styleable.AndroidManifestActivity_icon, 0);
                ai.theme = sa.getResourceId(R.styleable.AndroidManifestActivity_theme, 0);
                ai.launchMode = sa.getInteger(R.styleable.AndroidManifestActivity_launchMode, 0);
                //noinspection ResourceType
                ai.screenOrientation = sa.getInt(
                        R.styleable.AndroidManifestActivity_screenOrientation,
                        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

                ai.configChanges = sa.getInt(R.styleable.AndroidManifestActivity_configChanges, 0);
                ai.softInputMode = sa.getInteger(
                        R.styleable.AndroidManifestActivity_windowSoftInputMode, 0);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    boolean hardwareAccelerated = sa.getBoolean(
                            R.styleable.AndroidManifestActivity_hardwareAccelerated,
                            mUsesHardwareAccelerated);
                    if (hardwareAccelerated) {
                        ai.flags |= ActivityInfo.FLAG_HARDWARE_ACCELERATED;
                    }
                }

                activities.add(ai);

                sa.recycle();

                // <intent-filter ...
                List<IntentFilter> intents = new ArrayList<IntentFilter>();
                int outerDepth = parser.getDepth();
                while ((type = parser.next()) != XmlResourceParser.END_DOCUMENT
                        && (type != XmlResourceParser.END_TAG
                        || parser.getDepth() > outerDepth)) {
                    if (type == XmlResourceParser.END_TAG || type == XmlResourceParser.TEXT) {
                        continue;
                    }

                    if (parser.getName().equals("intent-filter")) {
                        IntentFilter intent = new IntentFilter();

                        parseIntent(res, parser, attrs, true, true, intent);

                        if (intent.countActions() == 0) {
                            Log.w(TAG, "No actions in intent filter at "
                                    + mPluginFile + " "
                                    + parser.getPositionDescription());
                        } else {
                            intents.add(intent);
                            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER)) {
                                mLauncherActivityName = ai.name;
                            }
                        }
                    }
                }

                if (intents.size() > 0) {
                    if (mIntentFilters == null) {
                        mIntentFilters = new ConcurrentHashMap<String, List<IntentFilter>>();
                    }
                    mIntentFilters.put(ai.name, intents);
                }
            }

            int N = activities.size();
            if (N > 0) {
                mPackageInfo.activities = new ActivityInfo[N];
                mPackageInfo.activities = activities.toArray(mPackageInfo.activities);
            }
            return true;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    private static final String ANDROID_RESOURCES
            = "http://schemas.android.com/apk/res/android";

    private boolean parseIntent(Resources res, XmlResourceParser parser, AttributeSet attrs,
                                boolean allowGlobs, boolean allowAutoVerify, IntentFilter outInfo)
            throws XmlPullParserException, IOException {
        TypedArray sa;
        int outerDepth = parser.getDepth();
        int type;
        while ((type = parser.next()) != XmlResourceParser.END_DOCUMENT
                && (type != XmlResourceParser.END_TAG || parser.getDepth() > outerDepth)) {
            if (type == XmlResourceParser.END_TAG || type == XmlResourceParser.TEXT) {
                continue;
            }

            String nodeName = parser.getName();
            if (nodeName.equals("action")) {
                String value = attrs.getAttributeValue(
                        ANDROID_RESOURCES, "name");
                if (value == null || value.length() == 0) {
                    return false;
                }
                skipCurrentTag(parser);

                outInfo.addAction(value);
            } else if (nodeName.equals("category")) {
                String value = attrs.getAttributeValue(
                        ANDROID_RESOURCES, "name");
                if (value == null || value.length() == 0) {
                    return false;
                }
                skipCurrentTag(parser);

                outInfo.addCategory(value);

            } else if (nodeName.equals("data")) {
                sa = res.obtainAttributes(attrs,
                        R.styleable.AndroidManifestData);

                String str = sa.getString(
                        R.styleable.AndroidManifestData_mimeType);
                if (str != null) {
                    try {
                        outInfo.addDataType(str);
                    } catch (IntentFilter.MalformedMimeTypeException e) {
                        sa.recycle();
                        return false;
                    }
                }

                str = sa.getString(
                        R.styleable.AndroidManifestData_scheme);
                if (str != null) {
                    outInfo.addDataScheme(str);
                }

                String host = sa.getString(
                        R.styleable.AndroidManifestData_host);
                String port = sa.getString(
                        R.styleable.AndroidManifestData_port);
                if (host != null) {
                    outInfo.addDataAuthority(host, port);
                }

                str = sa.getString(
                        R.styleable.AndroidManifestData_path);
                if (str != null) {
                    outInfo.addDataPath(str, PatternMatcher.PATTERN_LITERAL);
                }

                str = sa.getString(
                        R.styleable.AndroidManifestData_pathPrefix);
                if (str != null) {
                    outInfo.addDataPath(str, PatternMatcher.PATTERN_PREFIX);
                }

                str = sa.getString(
                        R.styleable.AndroidManifestData_pathPattern);
                if (str != null) {
                    if (!allowGlobs) {
                        return false;
                    }
                    outInfo.addDataPath(str, PatternMatcher.PATTERN_SIMPLE_GLOB);
                }

                sa.recycle();
                skipCurrentTag(parser);
            } else {
                return false;
            }
        }

        return true;
    }

    private static void skipCurrentTag(XmlResourceParser parser)
            throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        int type;
        while ((type = parser.next()) != XmlResourceParser.END_DOCUMENT
                && (type != XmlResourceParser.END_TAG
                || parser.getDepth() > outerDepth)) {
        }
    }

    private static String buildClassName(String pkg, CharSequence clsSeq) {
        if (clsSeq == null || clsSeq.length() <= 0) {
            return null;
        }
        String cls = clsSeq.toString();
        char c = cls.charAt(0);
        if (c == '.') {
            return (pkg + cls).intern();
        }
        if (cls.indexOf('.') < 0) {
            StringBuilder b = new StringBuilder(pkg);
            b.append('.');
            b.append(cls);
            return b.toString().intern();
        }
        if (c >= 'a' && c <= 'z') {
            return cls.intern();
        }
        return null;
    }

    public PackageInfo getPackageInfo() {
        return mPackageInfo;
    }

    public String getSourcePath() {
        return mPluginFile;
    }

    public ConcurrentHashMap<String, List<IntentFilter>> getIntentFilters() {
        return mIntentFilters;
    }

    public String getLibraryDirectory() {
        return mLibDir;
    }

    /**
     * This method tells whether the bundle has `resources.arsc` entry, note that
     * it doesn't make sense until your bundle was built by `gradle-small` 0.9.0 or above.
     *
     * @return <tt>true</tt> if doesn't have any resources
     */
    public boolean isNonResources() {
        return mNonResources;
    }

    protected void close() {
        mReadBuffer = null;
    }

//    protected boolean is64Bits() {
//        String nativeDir = Small.getContext().getApplicationInfo().nativeLibraryDir;
//        if (nativeDir != null && nativeDir.isEmpty()) {
//            return nativeDir.contains("64");
//        }
//
//        return false;
//    }

    public String buildVersion() {
        return mBuildVersion;
    }

    public String buildDate() {
        return mBuildDate;
    }

    public HashMap<String, String> dependedInfo() {
        return mDependedInfo;
    }
}
