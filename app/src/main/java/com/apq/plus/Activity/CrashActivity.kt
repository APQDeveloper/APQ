package com.apq.plus.Activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.text.Html
import android.widget.TextView
import com.apq.plus.Base.BaseActivity
import com.apq.plus.R

class CrashActivity : BaseActivity(R.layout.activity_crash) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val errorMsg: TextView = findViewById(R.id.error_msg)
        errorMsg.text = if (Build.VERSION.SDK_INT >= 24) Html.fromHtml(intent.getStringExtra("errors"),Html.FROM_HTML_OPTION_USE_CSS_COLORS) else Html.fromHtml(intent.getStringExtra("errors"))

        val fabRestart: FloatingActionButton = findViewById(R.id.fab)
        fabRestart.setOnClickListener {
            startActivity(Intent(this,LaunchActivity::class.java))
            finish()

        }

        errorMsg.setOnClickListener {
            (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).primaryClip = ClipData.newHtmlText("APQ Crash Logs",errorMsg.text,intent.getStringExtra("errors"))
            Snackbar.make(fabRestart,R.string.base_copied,Snackbar.LENGTH_SHORT).show()
        }
    }
}
