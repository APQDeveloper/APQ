package com.apq.plus.Activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.LinearLayout
import com.apq.plus.Adapter.FileAdapter
import com.apq.plus.Base.BaseActivity
import com.apq.plus.R
import com.apq.plus.Utils.mDivisionDecoration
import jp.wasabeef.recyclerview.animators.FadeInAnimator
import java.io.File

class FileChooserActivity : BaseActivity() {
    lateinit var recyclerView: RecyclerView
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var fab: FloatingActionButton
    var adapter: FileAdapter? = null
    lateinit var loadingMsg: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_chooser)
        recyclerView = findViewById(R.id.recycler_view)
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        fab = findViewById(R.id.fab)
        fab.visibility = View.INVISIBLE

        adapter = object: FileAdapter(Environment.getExternalStorageDirectory(),Environment.getExternalStorageDirectory().path){

            override fun sort(dest: File): ArrayList<File> {
                //在另一线程展开
                runOnUiThread {
                    swipeRefreshLayout.isRefreshing = true
                    val fadeOut = AlphaAnimation(1f,0f)
                    fadeOut.duration = 300
                    fadeOut.fillAfter = true
                    recyclerView.startAnimation(fadeOut)
                }
                val list = ArrayList<File>(dest.listFiles().toList().sorted())
                val fileList = ArrayList<File>()
                var i = 0
                while (i<list.size){
                    if (list[i].isFile){
                        fileList.add(list[i])
                        list.removeAt(i)
                        i--
                    }
                    i++
                }
                list.addAll(fileList)
                runOnUiThread {
                    swipeRefreshLayout.isRefreshing = false
                    val fadeIn = AlphaAnimation(0f,1f)
                    fadeIn.duration = 300
                    fadeIn.fillAfter = true
                    recyclerView.startAnimation(fadeIn)
                }
                return list
            }
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this@FileChooserActivity,LinearLayoutManager.VERTICAL,false)
        recyclerView.itemAnimator = FadeInAnimator()
        recyclerView.addItemDecoration(mDivisionDecoration(this@FileChooserActivity))

        swipeRefreshLayout.setColorSchemeColors(resources.getColor(R.color.colorAccent))
        swipeRefreshLayout.setOnRefreshListener {
            Handler().postDelayed({
                if (adapter != null){
                    adapter!!.refresh(this)
                }
            },500)
        }

        adapter!!.setSelectStateChangeListener { file, states ->
            if (states == FileAdapter.States.SELECTED){
                fab.show()
            }
            else{
                fab.hide()
            }
        }
        fab.setOnClickListener {
            val intent = Intent()
            intent.putExtra("fileReturn",adapter!!.selectedItem!!.path)
            setResult(Activity.RESULT_OK,intent)
            finish()
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item!!.itemId == android.R.id.home){
            onBackPressed()
        }
        return true
    }
}
