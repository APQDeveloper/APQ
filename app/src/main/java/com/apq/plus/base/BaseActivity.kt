package com.apq.plus.base

import com.apq.plus.util.ActivityCollector
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.PersistableBundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.apq.plus.R

/**
 * 基类 合并自BaseActivity 和 AActivity
 */

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

    /**
     * 打印类型枚举
     */
    enum class Print {
        TOAST,
        SNACKBAR,
        DIALOG
    }

    /**
     * 打印字符串
     *
     * @param type    展示的类型TOAST: 弹出Toast提示,SNACKBAR: 弹出Snackbar提示,DIALOOG: 弹出Dialog提示框
     * @param time    展示时间,当展示类型为DIALOG时无效
     * @param message 标题以及正文0:正文,1:标题
     */
    fun print(type: Print, time: Int, vararg message: String) {
        if (message[0].isEmpty()) return
        //运行在UI线程，方便直接调用
        runOnUiThread {
            when (type) {
                BaseActivity.Print.TOAST -> toast(message[0], time)
                BaseActivity.Print.SNACKBAR -> print(message[0], time)
                BaseActivity.Print.DIALOG -> dialog(message[0], if (message.size == 2) message[1] else null)
            }
        }

    }

    fun toast(message: String, time: Int) {
        Toast.makeText(this, message, time).show()
    }

    fun print(message: String, time: Int) {
        Snackbar.make(window.decorView, message, time).show()
    }

    fun dialog(message: String, title: String?) {
        val builder = AlertDialog.Builder(this)
        if (title != null) builder.setTitle(title)
        builder.setPositiveButton(getString(R.string.base_back), null)
        builder.setMessage(message)
        builder.show()
    }
}