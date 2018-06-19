package com.apq.plus.Utils

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
        activities.forEach {
            if (!it.isFinishing)
                it.finish()
        }
    }

    fun finishByClass(cls: Class<*>){
        activities.forEach {
            if (it.javaClass == cls)
                it.finish()
        }
    }

    fun forEach(loop: (a: Activity)-> Unit){
        activities.forEach { loop(it) }
    }
}