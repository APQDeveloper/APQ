package com.apq.plus.Base

import android.app.Application
import android.content.Intent
import com.apq.plus.Activity.CrashActivity
import com.apq.plus.Env
import com.apq.plus.R

class Application : Application(), Thread.UncaughtExceptionHandler {
    override fun onCreate() {
        super.onCreate()
        //异常捕获
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread?, e: Throwable?) {
        val log = StringBuilder()
        log.append("<h5>At Thread ${t?.name}:${e?.localizedMessage}</h5>")
        log.append("<p>")
        e?.stackTrace?.forEach {
            log.append("${it.className}.<i>${it.methodName}</i>(<font color=\"${Env.getHexByColorID(this,R.color.colorAccent)}\"><i>${it.fileName}:${it.lineNumber}</i></font>)<br/>")
        }
        log.append("</p>")

        val intent = Intent()
        intent.setClass(applicationContext, CrashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("errors",log.toString())
        applicationContext.startActivity(intent)

        android.os.Process.killProcess(android.os.Process.myPid())
        System.exit(0)
    }
}