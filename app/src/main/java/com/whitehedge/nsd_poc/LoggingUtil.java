package com.whitehedge.nsd_poc;

import android.util.Log;

/**
 * Created by Mahesh Chakkarwar on 19-07-2016.
 */
public class LoggingUtil {
    public static void showError(String className, String msg) {
        Log.e(className, msg);
    }

    public static void showVerbose(String className, String msg) {
        Log.v(className, msg);
    }

    public static void showDebug(String className, String msg) {
        Log.d(className, msg);
    }
}
