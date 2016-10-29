package com.example.wsh.sudoku.util;


import android.util.Log;

/**
 * Created by wsh on 16-7-10.
 */
public final class Mylog {
    private static final String TAG = "mylog ";

    private Mylog() {}

    public static int i(String tag, String msg) {
        return Log.i(TAG + tag, msg);
    }

    public static int d(String tag, String msg) {
        return Log.d(TAG + tag, msg);
    }

    public static int e(String tag, String msg) {
        return Log.e(TAG + tag, msg);
    }

}
