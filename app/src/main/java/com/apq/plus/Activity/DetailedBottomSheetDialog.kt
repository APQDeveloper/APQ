package com.apq.plus.Activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v7.widget.AppCompatImageView
import android.util.Log
import android.view.View
import android.widget.TextView
import android.support.v7.widget.Toolbar
import android.widget.ImageView
import android.widget.RelativeLayout
import com.apq.plus.Env
import com.apq.plus.R
import com.apq.plus.VMObject
import java.io.File

class DetailedBottomSheetDialog(val context: Context,val vm: VMObject) {
    private val dialog = BottomSheetDialog(context)
    private val contentView: View = (context as Activity).layoutInflater.inflate(R.layout.bottom_sheet_dialog,null)

    init {
        dialog.setContentView(contentView)
    }

    fun show(){
        val toolbar: Toolbar = contentView.findViewById(R.id.toolbar)
        val icon: AppCompatImageView = contentView.findViewById(R.id.icon)
        val name: TextView = contentView.findViewById(R.id.name)
        val description: TextView = contentView.findViewById(R.id.description)
        val console: RelativeLayout = contentView.findViewById(R.id.advanced_console)

        val profile = vm.baseInfo
        if (profile.isNull){
            Env.makeErrorDialog(context,"Object Not Found",false)
            return
        }
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white)
        toolbar.setNavigationOnClickListener {
            dismiss()
        }
        name.text = profile.name
        if (profile.description.isNotEmpty()) {
            description.text = profile.description
        }
        else{
            description.visibility = View.GONE
            /*
            val params = name.layoutParams as RelativeLayout.LayoutParams
            params.addRule(RelativeLayout.CENTER_VERTICAL)
            name.layoutParams = params
            */
        }
        if (profile.icon != null) {
            icon.setImageBitmap(profile.icon)
            icon.scaleType = ImageView.ScaleType.CENTER_CROP
            val params = icon.layoutParams as CollapsingToolbarLayout.LayoutParams
            params.topMargin = 0
            params.bottomMargin = 0
            icon.layoutParams = params
        }
        else{
            icon.setImageResource(R.drawable.ic_photo_white)
            icon.scaleType = ImageView.ScaleType.FIT_CENTER
            val params = icon.layoutParams as CollapsingToolbarLayout.LayoutParams
            params.topMargin = 40
            params.bottomMargin = 40
            icon.layoutParams = params
        }

        (contentView.findViewById<AppCompatImageView>(R.id.edit)).setOnClickListener {
            val intent = Intent(context,VMEditActivity::class.java)
            intent.putExtra("dataToEdit", File(profile.file).readText())
            Log.i("VM Editor","Start editor by json.")
            (context as Activity).startActivityForResult(intent,0)
        }

        console.setOnClickListener {
            val intent = Intent(context,ConsoleActivity::class.java)
            intent.putExtra("port",vm.baseInfo.monitorPort)
            (context as Activity).startActivity(intent)
        }

        dialog.show()
    }

    fun dismiss(){
        dialog.dismiss()
    }

    fun destroy(){

    }
}