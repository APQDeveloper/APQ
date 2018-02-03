package com.apq.plus.app;

import android.app.Application;

import com.apq.plus.handler.CrashHandler;

/**
 * Application
 *
 * @author xhh
 */

public class APQApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
    }
}
