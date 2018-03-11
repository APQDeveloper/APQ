package com.apq.plus

import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.util.DisplayMetrics
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.apq.plus.Utils.VMProfile
import com.apq.plus.Utils.VMProfile.Units.*
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Created by zhufu on 2/5/18.
 */
object Env {
    //屏幕
    var screenWidth: Int = 0
    var screenHeight: Int = 0
    fun getScreenSize(metrics: DisplayMetrics){
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels
        Log.i("Screen","With = $screenWidth, Height = $screenHeight")
    }
    //虚拟机
    lateinit var APQDir: File

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
    /* 关闭软键盘 */
    fun closeSoftInput(context: Context?){
        if (context != null)
            (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)?.hideSoftInputFromWindow((context as Activity).window.peekDecorView().windowToken,0)
    }

    /**
     * 获取总内存大小
     * 单位MB
     */
    fun getTotalMemorySize(context: Context) : Double{
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)
        val mem = memInfo.totalMem
        Log.d("Memory","Totally $mem")
        val b = BigDecimal((mem/1024/1024).toString())

        return b.setScale(3,RoundingMode.HALF_UP).toDouble()
    }

    /* 单位转换 */
    fun convert(memory: VMProfile.Memory,toUnit: VMProfile.Units): VMProfile.Memory{
        var tmp = memory
        if (memory.unit.isBigger(toUnit)) {//如果转化到较小单位
            while (memory.unit != toUnit) {
                tmp = lastUnit(tmp)
            }
        }
        else {
            while (memory.unit != toUnit){
                tmp = nextUnit(tmp)
            }
        }
        return tmp
    }
    private fun nextUnit(memory: VMProfile.Memory): VMProfile.Memory{
        when (memory.unit){
            B -> memory.unit = KB
            KB -> memory.unit = MB
            MB -> memory.unit = GB
            else -> throw NullPointerException("NullUnit")
        }
        memory.size = BigDecimal(memory.size.toString()).div(1024.toBigDecimal()).toDouble()
        return memory
    }
    private fun lastUnit(memory: VMProfile.Memory): VMProfile.Memory{
        when (memory.unit){
            KB -> memory.unit = B
            MB -> memory.unit = KB
            GB -> memory.unit = MB
            else -> throw NullPointerException("NullUnit")
        }
        memory.size = BigDecimal(memory.size.toString()).multiply(1024.toBigDecimal()).toDouble()
        return memory
    }
}