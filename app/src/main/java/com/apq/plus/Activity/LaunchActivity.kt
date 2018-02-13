package com.apq.plus.Activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.AppCompatImageView
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.LinearLayout
import android.widget.TextView
import com.apq.plus.Base.BaseActivity
import com.apq.plus.Env
import com.apq.plus.R
import com.apq.plus.Utils.FileUtils
import com.apq.plus.Utils.ZipUtils
import java.io.File

val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

class LaunchActivity : BaseActivity() {

    lateinit var iconView : AppCompatImageView
    lateinit var loading: LinearLayout
    lateinit var loadingMsg: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)
        iconView = findViewById(R.id.icon)
        iconView.visibility = View.INVISIBLE
        loading = findViewById(R.id.loading_layout)
        loadingMsg = findViewById(R.id.progress_msg)
        loading.visibility = View.INVISIBLE

        request()
    }

    lateinit var file : File
    private fun initAPQ(){
        Env.APQDir = File("${filesDir.path}/APQ")
        if (Env.APQDir.exists() && proofreadAPQ()){
            iconView.visibility = View.VISIBLE
            Handler().postDelayed({
                val intent = Intent(this@LaunchActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            },200)
        }
        else {
            //显示Logo
            Env.getScreenSize(resources.displayMetrics)
            val animationShow = TranslateAnimation(0f,0f,
                    (Env.screenHeight-iconView.measuredHeight)/2f,0f)
            animationShow.duration = 500

            iconView.visibility = View.VISIBLE
            iconView.startAnimation(animationShow)

            //延时1s
            Handler().postDelayed({
                //移走Logo
                val animationMove = TranslateAnimation(0f,0f,0f,-(Env.screenHeight-iconView.measuredHeight)/2f-100)
                animationMove.duration = 500
                iconView.startAnimation(animationMove)

                animationMove.setAnimationListener(object : Animation.AnimationListener{
                    override fun onAnimationRepeat(animation: Animation?) {}

                    override fun onAnimationEnd(animation: Animation?) {
                        iconView.visibility = View.GONE

                        loading.visibility = View.VISIBLE
                        loading.startAnimation(animationShow)
                        loadingMsg.setText(R.string.progress_msg_extract)
                        animationShow.setAnimationListener(object : Animation.AnimationListener{
                            override fun onAnimationRepeat(animation: Animation?) {
                            }

                            override fun onAnimationEnd(animation: Animation?) {
                                //抽取raw/apq.zip
                                file = File("${filesDir.path}/APQ.zip")
                                file.writeBytes(resources.openRawResource(R.raw.apq).readBytes())

                                val dest = filesDir
                                ZipUtils.extractWithProgress(file,dest,handler,false,true)
                            }

                            override fun onAnimationStart(animation: Animation?) {
                            }
                        })
                    }
                    override fun onAnimationStart(animation: Animation?) {}
                })
            },1000)
        }
    }
    private val handler = Handler{
        when(it.what){
            ZipUtils.CompressStatus.HANDLING -> {
                val percent = it.data.getInt(ZipUtils.CompressStatus.Progress)

                loadingMsg.text = getString(R.string.progress_msg_deploy,percent.toString())

                Log.i("Progress",percent.toString())
            }
            ZipUtils.CompressStatus.DONE -> {
                file.delete()
                loadingMsg.setText(R.string.progress_msg_clean)

                loadingMsg.setText(R.string.progress_msg_proofread)
                Thread({
                    if (proofreadAPQ()) {
                        runOnUiThread{
                            loadingMsg.setText(R.string.progress_msg_done)
                        }

                        Looper.prepare()
                        Handler().postDelayed({
                            val intent = Intent(this@LaunchActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        },500)
                        Looper.loop()
                    }
                    else {
                        Looper.prepare()
                        Env.makeErrorDialog(this@LaunchActivity,getString(R.string.base_proofread_failed),true)
                        Looper.loop()
                    }
                }).start()
            }
            ZipUtils.CompressStatus.ERROR -> {
                val e = it.data.getString(ZipUtils.CompressStatus.Error)
                Env.makeErrorDialog(this@LaunchActivity,e,true)
            }
        }
        true
    }

    private fun proofreadAPQ(): Boolean{
        val listToBeProofread = FileUtils.listChildFile(Env.APQDir)
        var size : Double = 0.toDouble()
        listToBeProofread.forEach { size += FileUtils.getFileSize(it,FileUtils.FileSizeUnits.MB) }

        return listToBeProofread.size >= 210 && size>=72
    }

    private fun request() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return
        ActivityCompat.requestPermissions(this, permissions, 1)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initAPQ()
        } else {
            Snackbar.make(findViewById(R.id.icon), R.string.user_permissions_requested, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.base_ok, {
                        request()
                    })
                    .show()
        }
    }
}
