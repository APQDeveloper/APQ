package com.apq.plus.Activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.TextInputEditText
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.apq.plus.Adapter.DiskAdapter
import com.apq.plus.Base.BaseActivity
import com.apq.plus.R
import com.apq.plus.Utils.AppBarStateListener
import com.apq.plus.Utils.DiskAdapterDecoration
import com.apq.plus.Utils.VMProfile
import jp.wasabeef.recyclerview.animators.FadeInUpAnimator
import java.io.File

class VMEditActivity : BaseActivity() {

    private var result = VMProfile.emptyObject
    private lateinit var diskAdapter: DiskAdapter
    private lateinit var diskRecyclerView: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vmedit)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setTitle(R.string.base_basic_info)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        init()
    }

    private fun init(){
        loadData()

        val appBarLayout = findViewById<AppBarLayout>(R.id.app_bar)
        diskRecyclerView = findViewById(R.id.disk_recycler_view)

        appBarLayout.addOnOffsetChangedListener(object : AppBarStateListener(){
            override fun onStateChanged(appBarLayout: AppBarLayout, state: State) {
                when(state){
                    AppBarStateListener.State.EXPANDED -> {
                        //展开
                        supportActionBar!!.setTitle(R.string.base_basic_info)
                        Log.d("AppBar","Expanded")
                    }
                    AppBarStateListener.State.COLLAPSED -> {
                        //折叠
                        supportActionBar!!.setTitle(R.string.activity_vm_editor)
                        Log.d("AppBar","Collapsed")
                    }
                    else -> {
                        //中间
                        //supportActionBar!!.setTitle(R.string.activity_vm_editor)
                        Log.d("AppBar","In-between")
                    }
                }
            }
        })

        //disk
        diskAdapter = DiskAdapter(result.disks!!)
        diskRecyclerView.adapter = diskAdapter
        diskRecyclerView.layoutManager = LinearLayoutManager(this)
        diskRecyclerView.itemAnimator = FadeInUpAnimator()
        diskRecyclerView.addItemDecoration(DiskAdapterDecoration(this))
        diskAdapter.setAddItemInterface { type: String ->
            showEditDialog(VMProfile.DiskHolder.Disk(null,null,type),this,{ disk: VMProfile.DiskHolder.Disk?, result: Result ->
                if (result == Result.OK)
                    diskAdapter.add(disk!!)
            })
        }
        diskAdapter.setOnItemClickListener { view, pos ->
            showEditDialog(diskAdapter.disk.get(pos),this,{ disk: VMProfile.DiskHolder.Disk?, result: Result ->
                when(result){
                    Result.OK -> diskAdapter.change(pos,disk!!)
                    VMEditActivity.Result.DELETE -> diskAdapter.remove(pos)
                }
            })
        }
    }

    private var fileChooserResult: ((String) -> Unit)? = null
    enum class Result{
        CANCELED,DELETE,OK
    }
    /**
     * 弹出式编辑框
     * @param content 要编辑的内容，如果内容为$VMProfile.DiskHolder.emptyObject，那么将会取消删除按钮
     * @param context activity的context
     * @param onPostResult 结果，返还Result中的类型
     */
    @SuppressLint("SetTextI18n")
    private fun showEditDialog(content: VMProfile.DiskHolder.Disk,context: Context,onPostResult: (VMProfile.DiskHolder.Disk?,Result) -> Unit) {
        val type = content.label

        var result: VMProfile.DiskHolder.Disk
        val dialog = AlertDialog.Builder(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.layout_disk_adding, null)
        dialog.setCancelable(false)
        //Views on layout
        val fileChoose = dialogView.findViewById<Button>(R.id.disk_file_choose)
        val fileInput = dialogView.findViewById<TextInputEditText>(R.id.disk_file_input)
        val paramSuffixSpinner = dialogView.findViewById<Spinner>(R.id.disk_suffix_spinner)
        val paramPrefix = dialogView.findViewById<TextView>(R.id.disk_params_prefix)
        //Shared options
        if (!content.isEmpty) {
            fileInput.text = Editable.Factory.getInstance().newEditable(content.diskFile!!.path)
        }
        if (type != VMProfile.DiskHolder.CD) {
            val suffixes: CharRange =
                    if (type == VMProfile.DiskHolder.HardDisk) 'a'..'d'
                    else 'a'..'b'
            paramSuffixSpinner.adapter = ArrayAdapter<Char>(context, android.R.layout.simple_spinner_item, suffixes.toList())
            if (content.isEmpty) {
                //选择下一个字母
                val nextLetter = diskAdapter.disk.getNextUseAs(type!!)
                if (nextLetter != null)
                    paramSuffixSpinner.setSelection(nextLetter.toInt() - 97)
            }
            else if (content.useAs != null){
                val s = content.useAs!!.minus(97).toInt()
                paramSuffixSpinner.setSelection(s)
            }
        }

        dialog.setView(dialogView)
        dialog.setPositiveButton(R.string.base_ok, { _: DialogInterface, _: Int ->
            result = VMProfile.DiskHolder.Disk(File(fileInput.text.toString()), if (type != VMProfile.DiskHolder.CD) paramSuffixSpinner.selectedItem as Char
                    else null, type
            )
            onPostResult(result,Result.OK)
        })
        dialog.setNegativeButton(R.string.base_cancel, { _: DialogInterface, _: Int ->
            onPostResult(null,Result.CANCELED)
        })
        if (!content.isEmpty)
            dialog.setNeutralButton(R.string.user_delete,{ _: DialogInterface, _: Int ->
                onPostResult(null,Result.DELETE)
            })
        fileChoose.setOnClickListener{
            fileChooserResult = {
                fileInput.text = Editable.Factory.getInstance().newEditable(it)
                fileChooserResult = null
            }
            startActivityForResult(Intent(this@VMEditActivity,FileChooserActivity::class.java),1)
        }

        when (type) {
            VMProfile.DiskHolder.HardDisk -> {
                dialog.setTitle(context.getString(R.string.user_add, context.getString(R.string.base_raw_hard_disk)))
                paramPrefix.text = "hd"
            }
            VMProfile.DiskHolder.CD -> {
                dialog.setTitle(context.getString(R.string.user_add, context.getString(R.string.base_raw_cd_rom)))
                paramPrefix.text = "cdrom"
                paramSuffixSpinner.visibility = View.INVISIBLE
            }
            else -> {
                dialog.setTitle(context.getString(R.string.user_add, context.getString(R.string.base_raw_floppy_disk)))
                paramPrefix.text = "fd"
            }
        }
        dialog.show()
    }

    private fun loadData(){
        val data = intent.getStringExtra("dataToEdit")
        if (!data.isNullOrEmpty()){
            result = VMProfile.getVMProfileByJSON(data)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1){
            if (resultCode == Activity.RESULT_OK){
                if (fileChooserResult != null)
                    fileChooserResult!!(data!!.getStringExtra("fileReturn"))
            }
        }
    }
}