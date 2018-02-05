package com.apq.plus

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Toast
import java.io.File

/**
 * Created by zhufu on 2/5/18.
 */
object Env {
    //屏幕
    var screenWidth: Int = 0
    var screenHeight: Int = 0
    //虚拟机
    lateinit var APQDir: File

    fun getScreenSize(metrics: DisplayMetrics){
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels
        Log.i("Screen","With = $screenWidth, Height = $screenHeight")
    }

    fun makeErrorDialog(context: Context,e: String,isSerious: Boolean = false){
        val dialog = AlertDialog.Builder(context)

        dialog.setMessage(context.getString(R.string.base_error,e))
        dialog.setNegativeButton(R.string.user_close,{dialogInterface, which ->  (context as Activity).finish()})
        if (!isSerious)
            dialog.setPositiveButton(R.string.user_ignore,null)
        dialog.setNeutralButton(R.string.user_copy,{ dialogInterface, which ->
            val clip : ClipboardManager = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val data = ClipData.newPlainText("Error Message",e)

            clip.primaryClip = data
            Toast.makeText(context,R.string.base_copied,Toast.LENGTH_SHORT).show()
            if (isSerious)
                (context as Activity).finish()
        })

        dialog.show()
    }
}