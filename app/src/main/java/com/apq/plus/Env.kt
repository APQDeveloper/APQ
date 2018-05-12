package com.apq.plus

import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.DialogInterface
import android.os.Environment
import android.os.StatFs
import android.os.StrictMode
import android.text.format.Formatter
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import com.apq.plus.Activity.VMEditActivity
import com.apq.plus.Utils.VMProfile
import com.apq.plus.Utils.VMProfile.Units.*
import com.xw.repo.BubbleSeekBar
import eu.darken.rxshell.cmd.Cmd
import eu.darken.rxshell.cmd.RxCmdShell
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

/**
 * Created by zhufu on 2/5/18.
 * 环境
 */
object Env {
    //屏幕
    var screenWidth: Int = 0
    var screenHeight: Int = 0
    fun getScreenSize(metrics: DisplayMetrics){
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels
        Log.i("Screen","With = $screenWidth, Height = $screenHeight")
    }
    //虚拟机
    lateinit var APQDir: File
    lateinit var VMProfileDir: File

    fun makeErrorDialog(context: Context,e: String,isSerious: Boolean = false){
        val dialog = AlertDialog.Builder(context)

        dialog.setCancelable(!isSerious)
        dialog.setMessage(context.getString(R.string.base_error,e))
        dialog.setNegativeButton(R.string.user_close,{dialogInterface, which ->  (context as Activity).finish()})
        if (!isSerious)
            dialog.setPositiveButton(R.string.user_ignore,null)
        dialog.setNeutralButton(R.string.user_copy,{ dialogInterface, which ->
            val clip : ClipboardManager = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val data = ClipData.newPlainText("Error Message",e)

            clip.primaryClip = data
            Toast.makeText(context,R.string.base_copied,Toast.LENGTH_SHORT).show()
            if (isSerious)
                (context as Activity).finish()
        })

        dialog.show()
    }

    fun makeVMErrorDialog(context: Context,e: List<String>){
        val dialog = AlertDialog.Builder(context)

        dialog.setTitle(R.string.base_vm_error_title)
        val msg = StringBuilder()
        msg.append(context.getString(R.string.base_vm_error_content))
        msg.append('\n')
        e.forEach {
            msg.append("$it\n")
        }
        dialog.setMessage(msg.toString())

        dialog.setNegativeButton(R.string.user_dismiss,null)
        dialog.setNeutralButton(R.string.user_copy, { _: DialogInterface, _: Int ->
            val clip : ClipboardManager = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val data = ClipData.newPlainText("Error Message",e.toString())

            clip.primaryClip = data
            Toast.makeText(context,R.string.base_copied,Toast.LENGTH_SHORT).show()
        })

        dialog.show()
    }
    /* 系统 */
    /* 关闭软键盘 */
    fun closeSoftInput(context: Context?){
        if (context != null)
            (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)?.hideSoftInputFromWindow((context as Activity).window.peekDecorView().windowToken,0)
    }

    /**
     * 获取总内存大小
     * 单位MB
     */
    fun getTotalMemorySize(context: Context) : Double{
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)
        val mem = memInfo.totalMem
        Log.d("Memory","Totally $mem")
        val b = BigDecimal((mem/1024/1024).toString())

        return b.setScale(3,RoundingMode.HALF_UP).toDouble()
    }

    fun getSDAvailableSize(context: Context): VMProfile.Memory{
        val path = Environment.getExternalStorageDirectory()
        val stats = StatFs(path.path)
        val r = Formatter.formatFileSize(context,(stats.availableBlocksLong * stats.blockSizeLong))
        val f = r.indexOfFirst { it == ' ' }
        return VMProfile.Memory(r.substring(0,f).toDouble(), valueOf(r.substring(f+1)))
    }

    /* 单位转换 */
    fun convert(memory: VMProfile.Memory,toUnit: VMProfile.Units): VMProfile.Memory{
        var tmp = memory
        if (memory.unit.isBigger(toUnit)) {//如果转化到较小单位
            while (memory.unit != toUnit) {
                tmp = lastUnit(tmp)
            }
        }
        else {
            while (memory.unit != toUnit){
                tmp = nextUnit(tmp)
            }
        }
        return tmp
    }
    private fun nextUnit(memory: VMProfile.Memory): VMProfile.Memory{
        when (memory.unit){
            B -> memory.unit = KB
            KB -> memory.unit = MB
            MB -> memory.unit = GB
            else -> throw NullPointerException("NullUnit")
        }
        memory.size = BigDecimal(memory.size.toString()).div(1024.toBigDecimal()).toDouble()
        return memory
    }
    private fun lastUnit(memory: VMProfile.Memory): VMProfile.Memory{
        when (memory.unit){
            KB -> memory.unit = B
            MB -> memory.unit = KB
            GB -> memory.unit = MB
            else -> throw NullPointerException("NullUnit")
        }
        memory.size = BigDecimal(memory.size.toString()).multiply(1024.toBigDecimal()).toDouble()
        return memory
    }
    /* 获得系统架构 */
    val systemFramework : String?
    get() {
        val result = Cmd.builder("uname -a").execute(RxCmdShell.builder().build())
        if (result.exitCode == 0){
            val out = result.output.first()
            for (i in out.length-1 downTo 0){
                if (out[i] == ' '){
                    return out.substring(i+1)
                }
            }
        }
        return null
    }

    fun checkMD5(target: File?,md5File: File?): Boolean{
        if (target == null || md5File == null)
            return false
        val session = RxCmdShell.builder().open().blockingGet()
        session.submit(Cmd.builder("cd ${target.parent}").build()).blockingGet()
        val result = Cmd.builder("md5sum -c ${md5File.path}").execute(session)
        return result.exitCode == 0
    }

    fun switchNetThread() = StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build())


    //磁盘文件
    class DiskImg(val f: File) {
        val exists: Boolean
        get() = f.exists()
        enum class Format{
            raw,qcow2
        }

        fun create(format: Format,size: VMProfile.Memory): Cmd.Result?{
            if (f.exists() && f.isDirectory)
                if(!f.delete()) return Cmd.Result(null,1,listOf("Unable to create file"),listOf("$f already exists!"))
            val r = Cmd.builder("${APQDir.path}/bin/qemu-img create -f ${format.name} ${f.path} ${size.size}${size.unit.qemuName}").execute(RxCmdShell.builder().build())
            return r
        }
    }

    /**
     * 内存编辑框
     * @param context Context of Activity
     * @param default Default size
     * @param maxSize Largest size that user can change
     * @param onPostResult Called when User click 'Done'
     */
    fun showMemoryEditDialog(context: Context, default: VMProfile.Memory, maxSize: VMProfile.Memory, onPostResult: (VMProfile.Memory?, VMEditActivity.Result) -> Unit){
        val m: VMProfile.Memory = VMProfile.Memory(default.size,default.unit)
        val dialog = AlertDialog.Builder(context)
        dialog.setTitle(R.string.base_memory)
        val view = LayoutInflater.from(context).inflate(R.layout.layout_memory,null)
        dialog.setView(view)
        //控件
        val memoryUnit = view.findViewById<Spinner>(R.id.memory_unit)
        val memorySeekBar = view.findViewById<BubbleSeekBar>(R.id.memory_seek_bar)
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
            val memory = Env.convert(maxSize, m.unit)
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
            onPostResult(m, VMEditActivity.Result.OK)
        })
        dialog.setNegativeButton(R.string.base_cancel,{ _: DialogInterface, _: Int ->
            onPostResult(null, VMEditActivity.Result.CANCELED)
        })
        dialog.show()
    }
}