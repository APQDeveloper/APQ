package com.apq.plus.Utils

import android.content.Context
import android.graphics.Bitmap
import com.apq.plus.R
import com.google.gson.Gson
import java.io.File
import kotlin.collections.ArrayList

/**
 * Created by zhufu on 2/5/18.
 * 虚拟机配置文件对象
 */
class VMProfile(var name: String, var description: String, var icon: Bitmap? = null, var disks: DiskHolder?, var bootFrom: BootFrom,var memory: String, var extraHardware: HardwareHolder?, var useVnc: Boolean = true){
    class DiskHolder {
        /**
         * @param useAs 在虚拟机运行时的标签，如硬盘标为"-hdX $FILE_NAME"，中间'X'就是其值
         * @param label Companion Object中选填，用于标记磁盘类型
         */
        class Disk(var diskFile: File?, var useAs: Char?, var label: String?) {
            /**
             * 此参数不带'-'
             */
            val params: String?
                get() =
                    if (label.isNullOrEmpty())
                        null
                    else
                        when (label) {
                            HardDisk -> "hd$useAs"
                            FloppyDisk -> "fd$useAs"
                            else -> "cdrom"
                        }

            val isEmpty: Boolean
                get() = !((label == CD && diskFile != null) || (diskFile != null && useAs != null && useAs != ' '))

            override fun toString(): String = if (params != null) params!! else ""
        }
        companion object {
            const val HardDisk = "Hard Disk"
            const val FloppyDisk = "Floppy Disk"
            const val CD = "CD-Rom"
            val emptyObject = DiskHolder()

            fun getString(id: String,context: Context): String = when(id){
                VMProfile.DiskHolder.CD -> context.getString(R.string.base_raw_cd_rom)
                VMProfile.DiskHolder.HardDisk -> context.getString(R.string.base_raw_hard_disk)
                VMProfile.DiskHolder.FloppyDisk -> context.getString(R.string.base_raw_floppy_disk)
                else -> "Unknown"
            }

            fun getIcon(id: String): Int = when(id){
                VMProfile.DiskHolder.CD -> R.drawable.ic_disk
                VMProfile.DiskHolder.HardDisk -> R.drawable.ic_harddisk
                VMProfile.DiskHolder.FloppyDisk -> R.drawable.ic_floppy
                else -> 0
            }
        }

        val params: String?
        get() {
            val stringBuilder = StringBuilder()
            mList.forEach {
                val param = it.params
                if (!param.isNullOrEmpty())
                stringBuilder.append("-$param ")
            }
            return stringBuilder.toString()
        }

        private val mList = ArrayList<Disk>()
        /**
         * @return 返还该目标添加的位置（排序后），如果重复则返还-1
         */
        fun addDisk(disk: Disk): Int{
            //检查是否重复
            mList.forEach {
                if (it.label == disk.label && it.useAs == disk.useAs)
                    return -1
            }

            mList.add(disk)
            mList.sortBy {
                it.toString()
            }
            return mList.indexOf(disk)
        }

        fun get(p: Int) = mList[p]
        fun remove(p: Int) = mList.removeAt(p)
        val size: Int
        get() = mList.size

        fun getNextUseAs(label: String): Char?{
            if (label == CD)
                return null
            return when {
                mList.size <= 0 -> 'a'
                else -> {
                    val tmp = ArrayList<Char>()
                    mList.forEach {
                        if (it.label == label && it.useAs != null)
                            tmp.add(it.useAs!!)
                    }
                    val letterList = if (label == HardDisk) 'a'..'d' else 'a' .. 'b'

                    letterList.firstOrNull { !tmp.contains(it) }
                }
            }
        }

        fun forEach(t: (Disk) -> Unit){
            mList.forEach(t)
        }

        fun has(label: String): Boolean = mList.any { it.label == label && it.diskFile != null && it.diskFile!!.exists() }
    }

    class HardwareHolder{
        private val mList = ArrayList<Hardware>()

        class Hardware(val type: String,val model: String){
            companion object {
                val netModels = arrayListOf("ne2k_pci","i82551","i82557b","i82559er","rtl8139"
                        ,"e1000","pcnet","virtio","sungem")
                val soundModels = arrayListOf("sb16","es1370","ac97","adlib","gus","cs4231a","hda","pcspk")
                val vgaModels = arrayListOf("cirrus","none","qxl","std","vmware","xenfb")
            }
            init {
                val exception = ClassNotFoundException("Model doesn't match type.")
                when(type){
                    TYPE_VGA -> {
                        if (!vgaModels.contains(model))
                            throw exception
                    }
                    TYPE_NET -> {
                        if (!netModels.contains(model))
                            throw exception
                    }
                    TYPE_SOUND -> {
                        if (!soundModels.contains(model))
                            throw exception
                    }
                }
            }
            /**
             * 此参数不带'-'
             */
            val params: String
            get() = if (type != TYPE_CUSTOM) "$type $model" else model
        }
        companion object {
            const val TYPE_NET = "net"
            const val TYPE_SOUND = "soundhw"
            const val TYPE_VGA = "vga"
            const val TYPE_CUSTOM = "[custom]"
        }

        val size: Int
        get() = mList.size
        fun get(pos: Int) = mList[pos]

        val params: String
        get() {
            val stringBuilder = StringBuilder()
            mList.forEach {
                stringBuilder.append("-${it.params} ")
            }
            return stringBuilder.toString()
        }
    }

    /**
     * @param value 选填DiskHolder.Companion
     */
    class BootFrom(private var value: String?){
        val params: String?
        get() = if (value != null) "-boot ${if (value == DiskHolder.CD) 'd' else if (value == DiskHolder.HardDisk) 'c' else 'a'}"
                else null
        val isNullOrEmpty: Boolean
        get() = params.isNullOrEmpty()

        var boot: String?
        get() = if(value == DiskHolder.CD || value == DiskHolder.HardDisk || value == DiskHolder.FloppyDisk) value else null
        set(value) = if(value == DiskHolder.CD || value == DiskHolder.HardDisk || value == DiskHolder.FloppyDisk) this.value = value else this.value = null
    }

    companion object {
        fun getVMProfileByJSON(profile: String): VMProfile{
            val gson = Gson()
            return gson.fromJson(profile,VMProfile::class.java)
        }
        val emptyObject = VMProfile("","",null, DiskHolder.emptyObject,BootFrom(DiskHolder.CD),"64M",null,true)
    }

    override fun toString(): String {
        val gson = Gson()
        return gson.toJson(this)
    }
}