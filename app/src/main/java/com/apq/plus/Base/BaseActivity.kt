package com.apq.plus.Base

import Utils.ActivityCollector
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity

@SuppressLint("Registered")
/**
 * Created by zhufu on 2/3/18.
 */
open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        ActivityCollector.add(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityCollector.remove(this)
    }
}