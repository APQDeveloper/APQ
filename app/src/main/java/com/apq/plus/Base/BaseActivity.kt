package com.apq.plus.Base

import com.apq.plus.Utils.ActivityCollector
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import com.apq.plus.R

@SuppressLint("Registered")
/**
 * Created by zhufu on 2/3/18.
 */
open class BaseActivity(private val contentView: Int) : AppCompatActivity() {
    var nightMode = false;
    lateinit var contentViewGroup: View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCollector.add(this)

        contentViewGroup = LayoutInflater.from(this).inflate(contentView,null)
        setContentView(contentViewGroup)
        nightMode = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("night_mode",false)
        if (nightMode){
            contentViewGroup.setBackgroundColor(resources.getColor(R.color.cardview_dark_background))
        }
        else{
            contentViewGroup.setBackgroundColor(resources.getColor(R.color.cardview_light_background))
        }
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