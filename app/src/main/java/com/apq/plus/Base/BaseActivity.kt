package com.apq.plus.Base

import com.apq.plus.Utils.ActivityCollector
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.util.Log

@SuppressLint("Registered")
/**
 * Created by zhufu on 2/3/18.
 */
open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCollector.add(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityCollector.remove(this)
    }

    override fun attachBaseContext(newBase: Context?) {
        if (newBase != null)
            super.attachBaseContext(mContextWrapper.wrap(newBase))
    }
}