package com.example.wsh.sudoku;


import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;

/**
 * Created by wsh on 16-7-10.
 */
public class App extends Application {

    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
    }

    public static Context getContext() {
        return sContext;
    }
}
