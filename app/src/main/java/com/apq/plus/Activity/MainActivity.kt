package com.apq.plus.Activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.view.View
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.apq.plus.Adapter.VMAdapter
import com.apq.plus.Env

import com.apq.plus.R
import com.apq.plus.Utils.Differ
import com.apq.plus.Utils.VMProfile
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import jp.wasabeef.recyclerview.animators.FadeInAnimator

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var recyclerView: RecyclerView
    lateinit var fab: FloatingActionButton
    private var mainAdapter: VMAdapter? = null
    private var VMList = ArrayList<VMProfile>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        recyclerView = findViewById(R.id.main_recycler_view)
        recyclerView.itemAnimator = FadeInAnimator()
        fab = findViewById(R.id.fab)

        fab.setOnClickListener {
            val intent = Intent(this,VMEditActivity::class.java)
            startActivityForResult(intent,0)
        }

        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)

        refreshProfileData()

        val refresh : SwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        refresh.setColorSchemeColors(resources.getColor(R.color.colorAccent))
        refresh.setOnRefreshListener {
            Thread({
                Thread.sleep(300)
                refreshProfileData()
                refresh.isRefreshing = false
            }).start()
        }

        if (mainAdapter != null){
            mainAdapter!!.setItemOnClickListener {
                val intent = Intent(this,VMEditActivity::class.java)
                intent.putExtra("dataToEdit",it.toString())
                Log.i("VM Editor","Start editor by json.")
                startActivityForResult(intent,0)
            }
        }

        val sp = getSharedPreferences("app", Context.MODE_PRIVATE)
        if(!sp.getBoolean("isWelcomeShown",false)){
            //如果尚未展示欢迎标语
            TapTargetView.showFor(this, TapTarget
                    .forView(fab,getString(R.string.base_welcome_t),getString(R.string.base_welcome_s))
                    .transparentTarget(true))
            val e = sp.edit()
            e.putBoolean("isWelcomeShown",true)
            e.apply()
        }
    }

    private fun refreshProfileData(){
        val old = ArrayList(VMList)
        VMList.clear()
        Env.VMProfileDir.listFiles()?.forEach {
            if (it.isFile && it.canRead()){
                val gson = Gson()
                try {
                    VMList.add(gson.fromJson(it.readText(),VMProfile::class.java))
                }catch (e: JsonSyntaxException){
                    e.printStackTrace()
                    Env.makeErrorDialog(this,e.toString())
                }
            }
        }

        if (mainAdapter == null)
            mainAdapter = VMAdapter(VMList)

        runOnUiThread {
            if (recyclerView.adapter == null || recyclerView.layoutManager == null) {
                recyclerView.adapter = mainAdapter
                recyclerView.layoutManager = LinearLayoutManager(this)
            }
            else{
                Differ.update({
                    mainAdapter!!.mList = VMList
                }, old,VMList, mainAdapter!!)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 0 && resultCode == Activity.RESULT_OK){
            refreshProfileData()
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
