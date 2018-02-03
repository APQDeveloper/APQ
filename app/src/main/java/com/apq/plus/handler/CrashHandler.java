package com.apq.plus.handler;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.apq.plus.R;
import com.apq.plus.activity.BugActivity;
import com.apq.plus.app.APPManager;
import com.apq.plus.util.ExtrasUtil;

/**
 * 全局异常捕获
 *
 * @author xhh
 */

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static CrashHandler INSTANCE;
    private Context mContext;

    private static final String TAG_CRASH = "NULCrash";

    private CrashHandler() {

    }

    public void init(Context context) {
        this.mContext = context;
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public static synchronized CrashHandler getInstance() {
        if (INSTANCE == null) INSTANCE = new CrashHandler();
        return INSTANCE;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        Log.e(TAG_CRASH, mContext.getString(R.string.log_message_nulcrash) + throwable.getMessage());
        Intent intent = new Intent(mContext, BugActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(ExtrasUtil.EXTRA_BUG_THROWABLE, throwable);
        mContext.startActivity(intent);
        APPManager.getInstance().exitApp();
    }
}
