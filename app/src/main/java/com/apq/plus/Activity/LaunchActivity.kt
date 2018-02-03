package com.apq.plus.Activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.view.View
import com.apq.plus.R

val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

class LaunchActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)

        request()
    }

    private fun request(){
        ActivityCompat.requestPermissions(this, permissions,1)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(this@LaunchActivity, MainActivity::class.java)
            startActivity(intent)
        }
        else {
            Snackbar.make(findViewById(R.id.icon),R.string.user_permissions_requested,Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.base_ok,{
                        request()
                    })
                    .show()
        }
    }
}
