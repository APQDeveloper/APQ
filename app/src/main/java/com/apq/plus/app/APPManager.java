package com.apq.plus.app;

import android.app.Activity;

import java.util.LinkedList;
import java.util.List;

/**
 * 应用activity管理
 *
 * @author xhh
 */

public class APPManager {
    private static List<Activity> mActivities = new LinkedList<>();
    private static APPManager INSTANCE;

    private APPManager() {

    }

    public static synchronized APPManager getInstance() {
        if (INSTANCE == null) INSTANCE = new APPManager();
        return INSTANCE;
    }

    public void addActivity(Activity activity) {
        this.mActivities.add(activity);
    }

    public void removeActivity(Activity activity) {
        this.mActivities.remove(activity);
    }

    public void finishAllActivity() {
        if (mActivities != null) {
            for (int i = 0; i < mActivities.size(); i++) {
                if (mActivities.get(i) == null) continue;
                mActivities.get(i).finish();
            }
            mActivities.clear();
        }
    }

    public void exitApp() {
        finishAllActivity();
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
