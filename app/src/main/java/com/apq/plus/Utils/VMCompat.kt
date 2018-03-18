package com.apq.plus.Utils

import com.google.gson.Gson

/**
 * Created by zhufu on 3/17/18.
 */
object VMCompat {
    fun getVMProfileByJSON(profile: String): VMProfile{
        val gson = Gson()
        return gson.fromJson(profile,VMProfile::class.java)
    }
}