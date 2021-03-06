package com.apq.plus.Activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.AppBarLayout
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.TextInputEditText
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.*
import com.apq.plus.Adapter.DiskAdapter
import com.apq.plus.Base.BaseActivity
import com.apq.plus.Env
import com.apq.plus.R
import com.apq.plus.Utils.*
import com.apq.plus.View.MaterialItemView
import com.apq.plus.View.TextInfo
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.xw.repo.BubbleSeekBar
import jp.wasabeef.recyclerview.animators.FadeInUpAnimator
import top.wefor.circularanim.CircularAnim
import java.io.File

class VMEditActivity : BaseActivity() {

    //Data & Result
    var result: VMProfile = VMProfile("","", VMProfile.CPU(VMProfile.CPU.FRAMEWORK_X86, "base"),null, VMProfile.DiskHolder.emptyObject, VMProfile.BootFrom(VMProfile.DiskHolder.CD), VMProfile.Memory(64.toDouble(), VMProfile.Units.MB),null,true)
    private var isSaving = false
    //View & Adapter
    private lateinit var diskAdapter: DiskAdapter
    private lateinit var diskRecyclerView: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vmedit)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setTitle(R.string.base_basic_info)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        showTapTargets(1)
        init()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("VM Editor","Recycling data...")
    }

    private fun showTapTargets(index: Int){
        val sp = getSharedPreferences("app", Context.MODE_PRIVATE)
        if (sp.getBoolean("isInstructionShown",false)){
            return
        }

        when (index) {
            1 -> TapTargetView.showFor(this,
                    TapTarget.forView(findViewById<View>(R.id.toolbar), getString(R.string.base_welcome_vmeditor_t), getString(R.string.base_welcome_vmeditor_s))
                            .targetCircleColor(R.color.colorAccent)
                    , object : TapTargetView.Listener() {
                override fun onTargetClick(view: TapTargetView?) {
                    super.onTargetClick(view)
                    showTapTargets(2)
                }
            })
            2 -> TapTargetView.showFor(this,
                    TapTarget.forView(findViewById<View>(R.id.snapshot),getString(R.string.base_instr_snapshot_t),getString(R.string.base_instr_snapshot_s))
                            .transparentTarget(true)
                    ,object : TapTargetView.Listener(){
                override fun onTargetClick(view: TapTargetView?) {
                    super.onTargetClick(view)
                    showTapTargets(3)
                }
            })
            3 -> TapTargetView.showFor(this,
                    TapTarget.forView(findViewById<View>(R.id.name),getString(R.string.base_instr_nAd_t),getString(R.string.base_instr_nAd_s))
                            .transparentTarget(false)
                            .targetCircleColor(R.color.colorAccent)
                    ,object : TapTargetView.Listener() {
                override fun onTargetClick(view: TapTargetView?) {
                    super.onTargetClick(view)
                    showTapTargets(4)
                }
            })
            4 -> TapTargetView.showFor(this,
                    TapTarget.forView(findViewById<View>(R.id.card_disk_icon),getString(R.string.base_instr_disk_t),getString(R.string.base_instr_disk_s))
                            .transparentTarget(true)
                    ,object : TapTargetView.Listener() {
                override fun onTargetClick(view: TapTargetView?) {
                    super.onTargetClick(view)
                    showTapTargets(5)
                }
            })
            5 -> TapTargetView.showFor(this,
                    TapTarget.forView(findViewById<View>(R.id.cpu).findViewById(R.id.title_shape),getString(R.string.base_instr_cpu_t),getString(R.string.base_instr_cpu_s))
                            .transparentTarget(true)
                    ,object : TapTargetView.Listener() {
                override fun onTargetClick(view: TapTargetView?) {
                    super.onTargetClick(view)
                    showTapTargets(6)
                }
            })
            6 -> TapTargetView.showFor(this,
                    TapTarget.forView(findViewById<View>(R.id.memory).findViewById<View>(R.id.title_shape),getString(R.string.base_instr_memory_t),getString(R.string.base_instr_memory_s))
                            .transparentTarget(true)
                    ,object : TapTargetView.Listener() {
                override fun onTargetClick(view: TapTargetView?) {
                    super.onTargetClick(view)
                    showTapTargets(7)
                }
            })
            7 -> {
                TapTargetView.showFor(this,
                    TapTarget.forView(findViewById<View>(R.id.fab_save),getString(R.string.base_instr_save_button_t),getString(R.string.base_instr_save_button_s))
                            .transparentTarget(true))
                val editor = sp.edit()
                editor.putBoolean("isInstructionShown",true)
                editor.apply()
            }
        }
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
        //name & description
        val name: TextInputEditText = findViewById(R.id.name)
        val description: TextInputEditText = findViewById(R.id.description)
        name.setText(result.name)
        description.setText(result.description)

        //disk
        diskAdapter = DiskAdapter(result.disks!!)
        diskRecyclerView.adapter = diskAdapter
        diskRecyclerView.layoutManager = LinearLayoutManager(this)
        diskRecyclerView.itemAnimator = FadeInUpAnimator()
        diskRecyclerView.addItemDecoration(DiskAdapterDecoration(this))
        diskAdapter.setAddItemInterface { type: String ->
            showDiskEditDialog(VMProfile.DiskHolder.Disk(null,null,type),this,{ disk: VMProfile.DiskHolder.Disk?, result: Result ->
                if (result == Result.OK)
                    diskAdapter.add(disk!!)
                this@VMEditActivity.result.disks = diskAdapter.disk
            })
        }
        diskAdapter.setOnItemClickListener { _, pos ->
            showDiskEditDialog(diskAdapter.disk.get(pos),this,{ disk: VMProfile.DiskHolder.Disk?, result: Result ->
                when(result){
                    Result.OK -> diskAdapter.change(pos,disk!!)
                    VMEditActivity.Result.DELETE -> diskAdapter.remove(pos)
                    else -> {}
                }
                this@VMEditActivity.result.disks = diskAdapter.disk
            })
        }

        //启动选项
        fun getTextInfoByBootFrom(bf: VMProfile.BootFrom): TextInfo
                = TextInfo(getString(R.string.base_boot_from),VMProfile.DiskHolder.getString(bf.boot!!,this),VMProfile.DiskHolder.getIcon(bf.boot!!))
        //控件
        val bootSelection = findViewById<MaterialItemView>(R.id.boot_from)
        bootSelection.set(getTextInfoByBootFrom(result.bootFrom))
        val bootError = findViewById<AppCompatImageView>(R.id.boot_error_alert)
        //控制错误提示，如果选定项不存在则弹出
        fun updateBootError() { bootError.visibility = if(result.disks!!.has(bootSelection.tag as String)) View.INVISIBLE else View.VISIBLE }
        diskAdapter.setOnItemChangeListener {
            updateBootError()
        }
        /* 对话框 */
        val bootErrorDialog = AlertDialog.Builder(this)
        bootErrorDialog.setTitle(R.string.base_useless_configuration)
        bootErrorDialog.setMessage(R.string.base_msg_ules_conf_cause)
        bootErrorDialog.setPositiveButton(R.string.base_ok,null)
        bootError.setOnClickListener {
            bootErrorDialog.show()
        }
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(R.layout.layout_boot_choose)
        //控件
        val cdChoice = bottomSheetDialog.findViewById<MaterialItemView>(R.id.to_choose_cd)
        val hardDiskChoice = bottomSheetDialog.findViewById<MaterialItemView>(R.id.to_choose_hard_disk)
        val floppyDiskChoice = bottomSheetDialog.findViewById<MaterialItemView>(R.id.to_choose_floppy_disk)

        fun updateSelection(){
            cdChoice!!.set(TextInfo(TextInfo.DO_NOT_CHANGE_TITLE, TextInfo.DO_NOT_CHANGE_TITLE,R.drawable.ic_disk))
            hardDiskChoice!!.set(TextInfo(TextInfo.DO_NOT_CHANGE_TITLE, TextInfo.DO_NOT_CHANGE_TITLE,R.drawable.ic_harddisk))
            floppyDiskChoice!!.set(TextInfo(TextInfo.DO_NOT_CHANGE_TITLE, TextInfo.DO_NOT_CHANGE_TITLE,R.drawable.ic_floppy))
            when(bootSelection.tag as String) {
                VMProfile.DiskHolder.CD -> {
                    cdChoice.set(TextInfo(TextInfo.DO_NOT_CHANGE_TITLE, TextInfo.DO_NOT_CHANGE_TITLE,R.drawable.ic_check))
                }
                VMProfile.DiskHolder.HardDisk -> {
                    hardDiskChoice.set(TextInfo(TextInfo.DO_NOT_CHANGE_TITLE, TextInfo.DO_NOT_CHANGE_TITLE,R.drawable.ic_check))
                }
                VMProfile.DiskHolder.FloppyDisk -> {
                    floppyDiskChoice.set(TextInfo(TextInfo.DO_NOT_CHANGE_TITLE, TextInfo.DO_NOT_CHANGE_TITLE,R.drawable.ic_check))
                }
            }
            //更新本地变量
            result.bootFrom.boot = bootSelection.tag as String
        }
        cdChoice!!.setOnClickListener {
            bootSelection.tag = VMProfile.DiskHolder.CD
            updateSelection()
            bottomSheetDialog.dismiss()
        }
        hardDiskChoice!!.setOnClickListener {
            bootSelection.tag = VMProfile.DiskHolder.HardDisk
            updateSelection()
            bottomSheetDialog.dismiss()
        }
        floppyDiskChoice!!.setOnClickListener {
            bootSelection.tag = VMProfile.DiskHolder.FloppyDisk
            updateSelection()
            bottomSheetDialog.dismiss()
        }

        bootSelection.setOnClickListener {
            bottomSheetDialog.show()
            //更新选项
            updateSelection()
        }
        //取消时更新bootSelection
        bottomSheetDialog.setOnDismissListener {
            bootSelection.set(getTextInfoByBootFrom(result.bootFrom))
            updateBootError()
        }

        //CPU选项
        fun getCpuTextInfoByObject(cpu: VMProfile.CPU) : TextInfo = TextInfo(TextInfo.DO_NOT_CHANGE_TITLE,"${cpu.framework}, ${cpu.model}",R.drawable.ic_cpu)
        val cpuChoice = findViewById<MaterialItemView>(R.id.cpu)
        cpuChoice.set(getCpuTextInfoByObject(result.cpu))
        cpuChoice.setOnClickListener {
            showCpuEditDialog(result.cpu,this,{ cpu: VMProfile.CPU?, result: Result ->
                if (result == Result.OK) {
                    this.result.cpu = cpu!!
                    cpuChoice.set(getCpuTextInfoByObject(cpu))
                }
            })
        }

        //内存选项
        val memoryChoice = findViewById<MaterialItemView>(R.id.memory)
        fun updateMemory(){
            val tmp = TextInfo(getString(R.string.base_memory),
                    getString(when(result.memory.unit){
                        VMProfile.Units.B -> R.string.base_units_b
                        VMProfile.Units.KB -> R.string.base_units_kb
                        VMProfile.Units.MB -> R.string.base_units_mb
                        VMProfile.Units.GB -> R.string.base_units_gb
                    },result.memory.size),TextInfo.DO_NOT_CHANG_RES)
            memoryChoice.set(tmp)
        }
        updateMemory()
        memoryChoice.setOnClickListener {
            showMemoryEditDialog(this,result.memory,{ s: VMProfile.Memory?, result: Result ->
                if (result == Result.OK){
                    this.result.memory = s!!
                    updateMemory()
                }else {
                    Log.i("Memory","Cancelled in ${this.result.memory.size}")
                }
            })
        }
        /* 保存按钮 */
        val fab = findViewById<FloatingActionButton>(R.id.fab_save)
        fab.setOnClickListener {
            val mask = findViewById<LinearLayout>(R.id.save_mask)
            saveData({
                CircularAnim.fullActivity(this,mask)
                        .colorOrImageRes(R.color.colorPrimaryLight)
                        .go {
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
            })
            val anim = AlphaAnimation(0f,0.7f)
            anim.duration = 300
            mask.alpha = 0.7f
            mask.visibility = View.VISIBLE
            CircularAnim.show(mask)
                    .triggerView(it)
                    .duration(300)
                    .go()
            mask.startAnimation(anim)
            (it as FloatingActionButton).hide()
        }
    }

    private fun saveData(onDone: ()->Unit){
        isSaving = true
        Env.closeSoftInput(this)
        /* Data Collection */
        val changeInsteadOfCreating : Boolean = !result.name.isEmpty()
        val oldName : String? = if (changeInsteadOfCreating) result.name else null

        val nameEdit = findViewById<TextInputEditText>(R.id.name)
        val descriptionEdit = findViewById<TextInputEditText>(R.id.description)
        result.name = nameEdit.text.toString()
        result.description = descriptionEdit.text.toString()
        if (result.name.isEmpty()){
            result.name = getString(R.string.base_unnamed)
        }
        /* Data Save */
        if (!Env.VMProfileDir.exists() || Env.VMProfileDir.isFile){
            Env.VMProfileDir.deleteRecursively()
            Env.VMProfileDir.mkdir()
        }
        if (changeInsteadOfCreating){
            val old = File("$filesDir/VMProfile/$oldName.json")
            old.delete()
        }
        val save = File("$filesDir/VMProfile/${result.name}.json")
        save.createNewFile()
        save.writeText(result.toString())
        if (save.exists()) {
            Handler().postDelayed({
                onDone()
            }, 500)
        }
        else{
            Env.makeErrorDialog(this,getString(R.string.base_error_vm_writing_failed))
        }
    }

    /**
     * 内存编辑框
     *
     */
    private fun showMemoryEditDialog(context: Context, default: VMProfile.Memory, onPostResult: (VMProfile.Memory?, Result) -> Unit){
        val m: VMProfile.Memory = VMProfile.Memory(default.size,default.unit)
        val dialog = AlertDialog.Builder(context)
        dialog.setTitle(R.string.base_memory)
        val view = LayoutInflater.from(context).inflate(R.layout.layout_memory,null)
        dialog.setView(view)
        //控件
        val memoryUnit = view.findViewById<Spinner>(R.id.memory_unit)
        val memorySeekBar = view.findViewById<BubbleSeekBar>(R.id.memory_seek_bar)
        val memorySize = Env.getTotalMemorySize(this)
        val adapter = ArrayAdapter<String>(context,android.R.layout.simple_spinner_item,
                listOf(context.getString(R.string.base_units_raw_b),context.getString(R.string.base_units_raw_kb),context.getString(R.string.base_units_raw_mb),context.getString(R.string.base_units_raw_gb)))
        memoryUnit.adapter = adapter
        memoryUnit.setSelection(when(m.unit){
            VMProfile.Units.B -> 0
            VMProfile.Units.KB -> 1
            VMProfile.Units.MB -> 2
            VMProfile.Units.GB -> 3
        })
        fun updateViews() {
            val memory = Env.convert(VMProfile.Memory(memorySize), m.unit)
            memorySeekBar.configBuilder
                    .showSectionMark()
                    .showSectionText()
                    .showThumbText()
                    .max(memory.size.toFloat())
                    .min(Env.convert(VMProfile.Memory(4.toDouble()), m.unit).size.toFloat())
                    .progress(m.size.toFloat())
                    .build()
        }
        memoryUnit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                m.updateUnit(VMProfile.Units.MB)
                updateViews()
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                m.updateUnit(
                    when (position){
                        0 -> VMProfile.Units.B
                        1 -> VMProfile.Units.KB
                        2 -> VMProfile.Units.MB
                        3 -> VMProfile.Units.GB
                        else -> VMProfile.Units.MB
                    })

                updateViews()
            }

        }
        memorySeekBar.onProgressChangedListener = object : BubbleSeekBar.OnProgressChangedListener{
            override fun onProgressChanged(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {
                m.size = progressFloat.toDouble()
            }

            override fun getProgressOnActionUp(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {
            }

            override fun getProgressOnFinally(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {

            }

        }
        updateViews()
        dialog.setPositiveButton(R.string.base_ok,{ _: DialogInterface, _: Int ->
            onPostResult(m,Result.OK)
        })
        dialog.setNegativeButton(R.string.base_cancel,{ _: DialogInterface, _: Int ->
            onPostResult(null,Result.CANCELED)
        })
        dialog.show()
    }

    /**
     * CPU编辑框
     * @param content 编辑内容
     * @param context activity的context
     * @param onPostResult 返还结果
     */
    private fun showCpuEditDialog(content: VMProfile.CPU,context: Context,onPostResult: (VMProfile.CPU?, Result) -> Unit){
        val dialog = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.layout_cpu_edit,null)
        dialog.setView(view)
        dialog.setTitle(R.string.user_edit_cpu)
        val radioGroup = view.findViewById<RadioGroup>(R.id.cpu_select_group)
        val x86 = view.findViewById<RadioButton>(R.id.cpu_select_x86)
        val x86_64 = view.findViewById<RadioButton>(R.id.cpu_select_x86_64)
        val arm = view.findViewById<RadioButton>(R.id.cpu_select_arm)
        val aarch64 = view.findViewById<RadioButton>(R.id.cpu_select_aarch64)
        val modelSpinner = view.findViewById<Spinner>(R.id.model_choice)

        var models: List<String> = listOf()
        fun updateModelSelections(frameworkId: Int){
            models = when(frameworkId){
                x86.id -> VMProfile.CPU.x86Models
                x86_64.id -> VMProfile.CPU.x86Models
                arm.id -> VMProfile.CPU.armMachines
                aarch64.id -> VMProfile.CPU.armMachines
                else -> VMProfile.CPU.x86Models
            }
            val modelAdapter = ArrayAdapter(this,android.R.layout.simple_spinner_item,models)
            modelSpinner.adapter = modelAdapter
        }
        fun selectModelItem(itemValue: String){
            modelSpinner.setSelection(models.indexOf(itemValue))
        }

        //选择已知项
        radioGroup.check(
                when (content.framework){
                    VMProfile.CPU.FRAMEWORK_X86 -> x86.id
                    VMProfile.CPU.FRAMEWORK_X86_64 -> x86_64.id
                    VMProfile.CPU.FRAMEWORK_ARM -> arm.id
                    VMProfile.CPU.FRAMEWORK_AARCH64 -> aarch64.id
                    else -> x86.id
                }
        )
        updateModelSelections(radioGroup.checkedRadioButtonId)
        selectModelItem(content.model)
        //监听
        radioGroup.setOnCheckedChangeListener({ _: RadioGroup, i: Int ->
            updateModelSelections(i)
        })

        //完成按钮
        dialog.setPositiveButton(R.string.base_ok,{ _: DialogInterface, _: Int ->
            //根据选项更新数据
            val cpu = VMProfile.CPU(
                when(radioGroup.checkedRadioButtonId){
                    x86.id -> VMProfile.CPU.FRAMEWORK_X86
                    x86_64.id -> VMProfile.CPU.FRAMEWORK_X86_64
                    arm.id -> VMProfile.CPU.FRAMEWORK_ARM
                    aarch64.id -> VMProfile.CPU.FRAMEWORK_AARCH64
                    else -> VMProfile.CPU.FRAMEWORK_X86
                }, modelSpinner.selectedItem as String)
            onPostResult(cpu,Result.OK)
        })
        dialog.show()
    }

    private var fileChooserResult: ((String) -> Unit)? = null
    enum class Result{
        CANCELED,DELETE,OK
    }
    /**
     * 磁盘编辑框
     * @param content 要编辑的内容，如果内容为$VMProfile.DiskHolder.emptyObject，那么将会取消删除按钮
     * @param context activity的context
     * @param onPostResult 结果，返还Result中的类型
     */
    @SuppressLint("SetTextI18n")
    private fun showDiskEditDialog(content: VMProfile.DiskHolder.Disk, context: Context, onPostResult: (VMProfile.DiskHolder.Disk?, Result) -> Unit) {
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
            Log.i("VM Editor","Init data from Intent.")
            result = VMCompat.getVMProfileByJSON(data)
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

    override fun onBackPressed() {
        if (!isSaving) {
            setResult(Activity.RESULT_CANCELED)
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item!!.itemId == android.R.id.home){
            onBackPressed()
        }
        return true
    }
}
