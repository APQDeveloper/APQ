package com.apq.plus.Activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.preference.SwitchPreference
import com.apq.plus.AppCompatPreferenceActivity
import com.apq.plus.Base.mContextWrapper
import com.apq.plus.Env
import com.apq.plus.R
import com.apq.plus.Utils.ActivityCollector
import eu.darken.rxshell.cmd.Cmd
import java.util.concurrent.TimeUnit

class SettingsActivity : AppCompatPreferenceActivity() {
    private companion object {
        var isAnyVMRunning: Boolean = false
        var needsViewReloading: Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()
        ActivityCollector.add(this)
        isAnyVMRunning = intent.getBooleanExtra("isAnyVMRunning",false)
        listView.setBackgroundColor(
                resources.getColor(
                        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("night_mode",false))
                            R.color.cardview_dark_background
                        else android.R.color.white))
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityCollector.remove(this)
    }

    private fun setupActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.base_action_settings)
    }

    override fun attachBaseContext(newBase: Context?) {
        if (newBase != null)
            super.attachBaseContext(mContextWrapper.wrap(newBase))
    }

    override fun isValidFragment(fragmentName: String?): Boolean {
        return fragmentName == GeneralPreferenceFragment::class.java.name
            || fragmentName == VMPreferenceFragment::class.java.name
            || fragmentName == QEMUPreferenceFragment::class.java.name
    }

    override fun onBuildHeaders(target: MutableList<Header>?) {
        //super.onBuildHeaders(target)
        loadHeadersFromResource(R.xml.pref_headers,target)
    }

    private var isInHeader = false
    override fun onHeaderClick(header: Header?, position: Int) {
        super.onHeaderClick(header, position)
        supportActionBar?.setTitle(header!!.titleRes)
        isInHeader = true
    }

    override fun onBackPressed() {
        if (isInHeader){
            supportActionBar?.setTitle(R.string.base_action_settings)
            super.onBackPressed()
            isInHeader = false
        }
        else {
            setResult(Activity.RESULT_OK,Intent().putExtra("needsViewReloading", needsViewReloading))
            finish()
        }
    }

    class GeneralPreferenceFragment : PreferenceFragment(){
        var mContext: Context? = null
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_general)

            val language = findPreference("lang")
            language.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { pref, _ ->
                if (pref.key == "lang"){
                    val dialog = AlertDialog.Builder(mContext)
                    dialog.setTitle(R.string.user_language)
                    dialog.setMessage(R.string.pref_restart_apply)
                    dialog.setPositiveButton(R.string.base_ok) { _: DialogInterface, _: Int ->
                        ActivityCollector.finishAll()
                        startActivity(Intent(mContext,LaunchActivity::class.java))
                    }
                    dialog.setNegativeButton(R.string.base_cancel,null)
                    dialog.show()
                }
                true
            }

            val nightMode = findPreference("night_mode")
            nightMode.setOnPreferenceClickListener {
                ActivityCollector.forEach { it.recreate() }
                activity.onBackPressed()
                true
            }
        }

        override fun onAttach(context: Context?) {
            super.onAttach(context)
            mContext = context!!
        }
    }

    class VMPreferenceFragment : PreferenceFragment(){
        var mContext: Context? = null
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_vm)

            val enableNoroot = findPreference("noroot_mode") as SwitchPreference
            enableNoroot.isEnabled = !SettingsActivity.isAnyVMRunning
            enableNoroot.setOnPreferenceClickListener {
                if (enableNoroot.isChecked) {
                    val dialog = AlertDialog.Builder(mContext)
                    dialog.setTitle(R.string.user_warn)
                    dialog.setMessage(R.string.base_msg_noroot)
                    dialog.setPositiveButton(R.string.base_ok,null)
                    dialog.show()
                }
                SettingsActivity.needsViewReloading = !SettingsActivity.needsViewReloading
                true
            }
        }

        override fun onAttach(context: Context?) {
            super.onAttach(context)
            mContext = context!!
        }
    }

    class QEMUPreferenceFragment : PreferenceFragment(){
        var mContext: Context? = null
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_qemu)

            val version = findPreference("qemu_version")
            version.isEnabled = false
            Env.QEMU("qemu-system-i386 --version").timeout(5,TimeUnit.SECONDS).subscribe { t: Cmd.Result? ->
                if (t?.exitCode == 0) {
                    val verBuilder = StringBuilder()
                    t.output?.forEach {
                        verBuilder.append("$it\n")
                    }
                    verBuilder.deleteCharAt(verBuilder.length - 1)

                    (mContext as Activity).runOnUiThread {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            version.isSingleLineTitle = false
                        }
                        version.isSelectable = true
                        version.title = verBuilder.toString()
                    }
                }
                else {
                    (mContext as Activity).runOnUiThread {
                        version.setTitle(R.string.base_msg_unkonwn)
                        version.title = "${version.title} \n${t?.errors.toString()}"
                    }
                }
            }

            val aarch = findPreference("qemu_aarch")
            aarch.title = getString(R.string.base_msg_system_aarch,Env.systemFramework)

            val reinstall = findPreference("qemu_reinstall")
            reinstall.isEnabled = !SettingsActivity.isAnyVMRunning
            reinstall.setOnPreferenceClickListener {
                it.isEnabled = false
                val dialog = AlertDialog.Builder(mContext)
                dialog.setTitle(getString(R.string.base_msg_deleting,getString(R.string.base_title_qemu)))
                dialog.setMessage(R.string.base_please_wait)
                dialog.setCancelable(false)
                val dialogWindow = dialog.create()
                dialogWindow.show()
                Thread {
                    if(!Env.APQDir.deleteRecursively()) {
                        (mContext as Activity).runOnUiThread {
                            dialogWindow.dismiss()
                            dialog.setMessage(R.string.base_msg_failed)
                            dialog.setCancelable(true)
                            dialog.show()
                            reinstall.isEnabled = true
                        }
                    } else {
                        ActivityCollector.finishAll()
                        startActivity(Intent(mContext, LaunchActivity::class.java))
                    }
                }.start()

                true
            }
        }

        override fun onAttach(context: Context?) {
            super.onAttach(context)
            mContext = context
        }
    }
}
