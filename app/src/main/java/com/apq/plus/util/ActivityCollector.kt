package com.apq.plus.util

import android.app.Activity

/**
 * Created by zhufu on 2/3/18.
 */
object ActivityCollector {
    private val activities = ArrayList<Activity>()

    fun add(activity: Activity){
        activities.add(activity)
    }
    fun remove(activity: Activity){
        activities.remove(activity)
    }

    fun finishAll(){
        activities.forEach { it.finish() }
    }

    fun exitApp() {
        finishAll()
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}