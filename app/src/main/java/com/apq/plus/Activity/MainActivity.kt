package com.apq.plus.Activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.design.widget.FloatingActionButton
import android.view.View
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.LinearLayout
import com.apq.plus.Adapter.VMAdapter
import com.apq.plus.Base.BaseActivity
import com.apq.plus.Env

import com.apq.plus.R
import com.apq.plus.Utils.Differ
import com.apq.plus.Utils.VMCompat
import com.apq.plus.VMObject
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.gson.JsonSyntaxException
import eu.darken.rxshell.cmd.Cmd
import jp.wasabeef.recyclerview.animators.FadeInAnimator
import timber.log.Timber
import java.util.*

class MainActivity : BaseActivity(R.layout.activity_main), NavigationView.OnNavigationItemSelectedListener {

    lateinit var recyclerView: RecyclerView
    lateinit var fab: FloatingActionButton
    private var mainAdapter: VMAdapter? = null
    var VMList = ArrayList<VMObject>()
    private var detailedBottomSheetDialog: DetailedBottomSheetDialog? = null
    private var norootMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setTitle(R.string.app_name)

        recyclerView = findViewById(R.id.main_recycler_view)
        recyclerView.itemAnimator = FadeInAnimator()
        fab = findViewById<FloatingActionButton>(R.id.fab)

        fun newEditor(){
            val intent = Intent(this,VMEditActivity::class.java)
            intent.putExtra("dataToEdit","new:${getNewID()}")
            startActivityForResult(intent,0)
        }
        fab.setOnClickListener {
            newEditor()
        }
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState > 0) {
                    fab.hide()
                } else {
                    fab.show()
                }
            }
        })

        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)

        refreshProfileData(false)

        val refresh : SwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        refresh.setColorSchemeColors(resources.getColor(R.color.colorAccent))
        refresh.setOnRefreshListener {
            Thread({
                Thread.sleep(300)
                refreshProfileData(false)
                runOnUiThread {
                    refresh.isRefreshing = false
                }
            }).start()
        }

        if (mainAdapter != null){
            mainAdapter!!.setItemOnClickListener {
                detailedBottomSheetDialog = DetailedBottomSheetDialog(this,it)
                detailedBottomSheetDialog!!.show()

                detailedBottomSheetDialog!!.setOnDismissedListener { isDataChanged ->
                    if (isDataChanged) refreshProfileData(false)
                    detailedBottomSheetDialog!!.recycle()
                }
            }

            mainAdapter!!.setStartListener {vm,onDone ->
                vm.setExceptionListener {
                    runOnUiThread {
                        Env.makeVMErrorDialog(this, listOf(it.toString()))
                    }
                }
                vm.run(
                        root = !norootMode,
                        onStarted =  {
                            runOnUiThread {
                                Snackbar.make(fab, getString(R.string.base_vm_running_snackbar, if (vm.baseInfo.useVNC) getString(R.string.base_display_vnc) else getString(R.string.base_display_xsdl), "127.0.0.1:${vm.baseInfo.videoPort}"), Snackbar.LENGTH_LONG).show()
                                onDone.invoke()
                            }
                        },
                        onDone = { r: Cmd.Result? ->
                            runOnUiThread {
                                onDone.invoke()
                                if (r != null && (r.exitCode != 0 || r.errors.size > 0)) {
                                    if (r.errors.indexOf("Permission denied") != -1){
                                        val dialog = AlertDialog.Builder(this)
                                        dialog.setTitle(R.string.base_vm_error_title)
                                        dialog.setMessage(R.string.base_msg_not_rooted)
                                        dialog.setPositiveButton(R.string.base_enable_no_root_mode,{ _,_->
                                            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("noroot_mode",true).apply()
                                            //norootMode = true
                                            refreshProfileData(true)
                                        })
                                        dialog.setNegativeButton(R.string.base_cancel,null)
                                        dialog.show()
                                    }
                                    else {
                                        Env.makeVMErrorDialog(this, r.errors)
                                    }
                                }
                            }
                        })
            }
            mainAdapter!!.setStopListener { vm,onDone ->
                vm.stop({
                    onDone.invoke()
                })
            }
        }

        val sp = getSharedPreferences("app", Context.MODE_PRIVATE)
        if(!sp.getBoolean("isWelcomeShown",false)){
            //如果尚未展示欢迎标语
            TapTargetView.showFor(this, TapTarget
                    .forView(fab,getString(R.string.base_welcome_t),getString(R.string.base_welcome_s))
                    .transparentTarget(true)
            ,object : TapTargetView.Listener() {
                override fun onTargetClick(view: TapTargetView?) {
                    Handler().postDelayed({
                        newEditor()
                    },300)
                    view!!.dismiss(false)
                }
            })
            val e = sp.edit()
            e.putBoolean("isWelcomeShown",true)
            e.apply()
        }
    }

    private fun refreshProfileData(forcedLoad: Boolean){
        val prefReader = PreferenceManager.getDefaultSharedPreferences(this)
        norootMode = prefReader.getBoolean("noroot_mode",false)

        val old = ArrayList(VMList)
        VMList.clear()
        Env.VMProfileDir.listFiles()?.forEach {
            if (it.isFile && it.canRead()){
                try {
                    VMList.add(VMObject(it))
                }catch (e: JsonSyntaxException){
                    e.printStackTrace()
                    Env.makeErrorDialog(this,e.toString())
                }
            }
        }

        VMList.sortBy { it.baseInfo.id }

        if (mainAdapter == null)
            mainAdapter = VMAdapter(VMList)

        runOnUiThread {
            if (recyclerView.adapter == null || recyclerView.layoutManager == null) {
                recyclerView.adapter = mainAdapter
                recyclerView.layoutManager = LinearLayoutManager(this)
            }
            else if (!forcedLoad){
                Differ.update({
                    mainAdapter!!.mList = VMList
                }, old,VMList, mainAdapter!!)
            }
            else {
                mainAdapter!!.mList = VMList
                mainAdapter!!.notifyDataSetChanged()
            }
            //额外一张标示“没有虚拟机”的卡片
            val card: LinearLayout = findViewById(R.id.noVM)
            if (VMList.size == 0){
                if (card.visibility == View.INVISIBLE) {
                    val ani = AlphaAnimation(0f, 1f)
                    ani.duration = 300
                    card.startAnimation(ani)
                }
                card.visibility = View.VISIBLE
            }
            else{
                if (card.visibility == View.VISIBLE) {
                    val ani = AlphaAnimation(1f, 0f)
                    ani.duration = 300
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
                }
                else{
                    card.visibility = View.INVISIBLE
                }
            }
        }
    }

    private fun getNewID(): Int{
        val tmp = VMList
        var max: Int = 0
        tmp.forEach {
            if (!it.baseInfo.isNull && it.baseInfo.id > max)
                max = it.baseInfo.id
        }
        for (i in 1 .. max){
            if (tmp.any { !it.baseInfo.isNull && it.baseInfo.id == i }){
                continue
            }
            else return i
        }
        return max +1
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 0 && resultCode == Activity.RESULT_OK){
            refreshProfileData(true)
            if (detailedBottomSheetDialog != null && !detailedBottomSheetDialog!!.isNullOrRecycled)
                detailedBottomSheetDialog!!.dismiss()
        }
        else if (requestCode == 1){
            if (data != null && data.getBooleanExtra("needsViewReloading",false)){
                refreshProfileData(true)
            }
        }
    }

    override fun onBackPressed() {
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        return if (id == R.id.action_settings) {
            startActivityForResult(Intent(this,SettingsActivity::class.java).putExtra("isAnyVMRunning",VMList.any { it.isRunning }),1)
            true
        } else super.onOptionsItemSelected(item)

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }
}
