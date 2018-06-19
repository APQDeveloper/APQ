package com.apq.plus.Activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputEditText
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.*
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.view.inputmethod.EditorInfo
import android.widget.Adapter
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import com.apq.plus.Adapter.FileAdapter
import com.apq.plus.Base.BaseActivity
import com.apq.plus.Env
import com.apq.plus.R
import com.apq.plus.Utils.VMProfile
import com.apq.plus.Utils.mDivisionDecoration
import jp.wasabeef.recyclerview.animators.FadeInAnimator
import java.io.File

class FileChooserActivity : BaseActivity(R.layout.activity_file_chooser),((File?, FileAdapter.States) -> Unit) {
    override fun invoke(f: File?, states: FileAdapter.States) {
        if (states == FileAdapter.States.SELECTED){
            fab.show()
        }
        else{
            fab.hide()
        }
    }

    lateinit var recyclerView: RecyclerView
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var fab: FloatingActionButton
    var adapter: FileAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recyclerView = findViewById(R.id.recycler_view)
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        fab = findViewById(R.id.fab)
        fab.visibility = View.INVISIBLE
        initToolbar()

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

        adapter!!.setSelectStateChangeListener(this)
        fab.setOnClickListener {
            val intent = Intent()
            intent.putExtra("fileReturn",adapter!!.selectedItem!!.path)
            setResult(Activity.RESULT_OK,intent)
            finish()
        }
    }

    private fun initToolbar(){
        val actionMenuView: ActionMenuView = findViewById(R.id.action_menu)
        setSupportActionBar(findViewById(R.id.toolbar))
        if (supportActionBar == null)
            return
        supportActionBar!!.setTitle(R.string.base_file_choosing)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val idCreate = actionMenuView.menu.add(getString(R.string.user_create,getString(R.string.user_new_disk_file))).itemId
        actionMenuView.setOnMenuItemClickListener {
            if (it.itemId == idCreate){
                initDiskCreating()
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun initDiskCreating(){
        //Views
        val name: TextInputEditText = findViewById(R.id.name)
        val formatSelector: AppCompatSpinner = findViewById(R.id.format)
        val card: CardView = findViewById(R.id.img_create_card)
        //Animations
        fun showCardLayout() {
            fab.hide()
            val ani = TranslateAnimation(0f, 0f, card.measuredHeight.toFloat(), 0f)
            ani.duration = 300
            card.visibility = View.VISIBLE
            card.startAnimation(ani)
        }
        fun hideCardLayout(){
            val ani = TranslateAnimation(0f, 0f, 0f, card.measuredHeight.toFloat())
            ani.duration = 300
            card.visibility = View.VISIBLE
            card.startAnimation(ani)
            ani.setAnimationListener(object : Animation.AnimationListener{
                override fun onAnimationRepeat(animation: Animation?) {
                }

                override fun onAnimationEnd(animation: Animation?) {
                    card.visibility = View.INVISIBLE
                }

                override fun onAnimationStart(animation: Animation?) {
                }
            })
            //还原初始状态
            this.adapter!!.select(-1)
            this.adapter!!.setSelectStateChangeListener(this)
        }
        if (card.visibility == View.VISIBLE){
            hideCardLayout()
            return
        }
        else{
            showCardLayout()
        }

        val formats = ArrayList<String>()
        Env.DiskImg.Format.values().forEach {
            formats.add(it.name)
        }
        val adapter = ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, formats)
        formatSelector.adapter = adapter
        formatSelector.setSelection(formats.indexOf("qcow2"))

        fun getName(): String = "${this.adapter!!.dest}/${name.text}.${formats[formatSelector.selectedItemId.toInt()]}"
        fun create(f: File,format: Env.DiskImg.Format,size: VMProfile.Memory){
            val r = Env.DiskImg(f).create(format,size)
            val e = StringBuilder()
            if (r == null){
                e.append("Unknown")
            }
            else if(r.exitCode == 1 || r.errors.size > 0){
                r.errors.forEach { e.append("$it\n") }
            }
            runOnUiThread {
                if (!e.isEmpty())
                    Env.makeErrorDialog(this,e.toString(),true)
                else{
                    val intent = Intent()
                    intent.putExtra("fileReturn",getName())
                    setResult(Activity.RESULT_OK,intent)
                    Snackbar.make(fab,R.string.progress_msg_done,Snackbar.LENGTH_INDEFINITE).show()
                    Handler().postDelayed({
                        finish()
                    },500)
                }
            }
        }
        name.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_GO || event.keyCode == KeyEvent.KEYCODE_ENTER){
                Env.showMemoryEditDialog(this, VMProfile.Memory(128.toDouble()),Env.getSDAvailableSize(this),{ memory: VMProfile.Memory?, result: VMEditActivity.Result ->
                    if (result == VMEditActivity.Result.OK){
                        Snackbar.make(fab,getString(R.string.base_creating,getString(R.string.user_new_disk_file)),Snackbar.LENGTH_INDEFINITE).show()
                        Thread({
                            create(File(getName())
                                    ,Env.DiskImg.Format.valueOf(formats[formatSelector.selectedItemId.toInt()]),memory!!)
                        }).start()
                    }
                })
            }
            true
        }
        //对现存选中项进行处理
        this.adapter!!.select(-1)
        this.adapter!!.setSelectStateChangeListener { file, states ->
            if (states == FileAdapter.States.SELECTED){
                name.setText(file!!.nameWithoutExtension)
            }
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
