package Utils

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
}