package com.apq.plus.Utils

import android.content.Context
import android.graphics.Bitmap
import com.apq.plus.Env
import com.apq.plus.R
import com.google.gson.Gson
import java.io.File
import kotlin.collections.ArrayList
import com.apq.plus.Utils.VMProfile.Units.*

/**
 * Created by zhufu on 2/5/18.
 * 虚拟机配置文件对象
 */
class VMProfile(var name: String, var description: String,var cpu: CPU, var icon: Bitmap? = null, var disks: DiskHolder?, var bootFrom: BootFrom,var memory: Memory, var extraHardware: HardwareHolder?, var useVnc: Boolean = true, var id: Int = -1){
    /**
     * 单位
     * 仅考虑到GB
     */
    enum class Units{
        B,KB,MB,GB;

        fun isBigger(other: Units): Boolean =
                when (other){
                    B -> this!= B
                    KB -> this!= B &&this!=KB
                    MB -> this!= B &&this!=KB&&this!=MB
                    GB -> false
                }
        val qemuName: Char
        get() = when (this){
                    B -> 'b'
                    KB -> 'k'
                    MB -> 'M'
                    GB -> 'G'
                }
    }

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
                            HardDisk -> "hd$useAs $diskFile"
                            FloppyDisk -> "fd$useAs $diskFile"
                            else -> "cdrom $diskFile"
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

        val params: String
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

        override fun equals(other: Any?): Boolean {
            return other is DiskHolder && (other.mList == mList)
        }
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

    /**
     * CPU 选项
     */
    class CPU(val framework: String, val model: String){
        init {
            val exception = ClassNotFoundException("Model doesn't match framework.")
            if (framework == FRAMEWORK_X86 || framework == FRAMEWORK_X86_64){
                if (!x86Models.contains(model))
                    throw exception
            }
            else if (framework == FRAMEWORK_ARM || framework == FRAMEWORK_AARCH64){
                if (!armMachines.contains(model))
                    throw exception
            }
            else throw NullPointerException("Framework not found.")
        }

        companion object {
            const val FRAMEWORK_X86 = "i386"
            const val FRAMEWORK_X86_64 = "x86_64"
            const val FRAMEWORK_ARM = "arm"
            const val FRAMEWORK_AARCH64 = "aarch64"
            //Models
            val x86Models = listOf("486","Broadwell","Broadwell-noTSX","Conroe","EPYC","Haswell","Haswell-noTSX"
                            ,"IvyBridge","Nehalem","Opteron_G1","Opteron_G2","Opteron_G3","Opteron_G4"
                            ,"Opteron_G5","Penryn","SandyBridge","Skylake-Client","Skylake-Server","Westmere"
                            ,"athlon","core2duo","coreduo","kvm32","kvm64","n270","pentium","pentium2","pentium3"
                            ,"phenom","qemu32","qemu64","base","host","max")
            val armMachines
                    = listOf    ("akita","ast2500-evb","borzoi","canon-a1100","cheetah","collie","connex","cubieboard"
                    ,"emcraft-sf2","highbank","imx25-pdk","integratorcp","kzm","lm3s6965evb","lm3s811evb"
                    ,"mainstone","midway","mps2-an385","mps2-an511","musicpal","n800","n810","netduino2"
                    ,"none","nuri","palmetto-bmc","raspi2","realview-eb","realview-eb-mpcore","realview-pb-a8"
                    ,"realview-pbx-a9","romulus-bmc","sabrelite","smdkc210","spitz","sx1","sx1-v1","terrier"
                    ,"tosa","verdex","versatileab","versatilepb","express-a15","vexpress-a9","virt-2.10","virt"
                    ,"virt-2.11","virt-2.6","virt-2.7","virt-2.8","virt-2.9","xilinx-zynq-a9","xlnx-ep108"
                    ,"xlnx-zcu102","z2")
        }
        val params: String
        get() = "cpu $model"
    }
    /* 内存 */
    class Memory(var size: Double = 0.toDouble(),var unit: Units = MB){
        fun updateUnit(unit: Units){
            size = Env.convert(this,unit).size
            this.unit = unit
        }
    }

    override fun toString(): String {
        val gson = Gson()
        return gson.toJson(this)
    }

    override fun equals(other: Any?): Boolean {
        if (other is VMProfile){
            return (other as VMProfile).toString() == this.toString()
        }
        return false
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + cpu.hashCode()
        result = 31 * result + (icon?.hashCode() ?: 0)
        result = 31 * result + (disks?.hashCode() ?: 0)
        result = 31 * result + bootFrom.hashCode()
        result = 31 * result + memory.hashCode()
        result = 31 * result + (extraHardware?.hashCode() ?: 0)
        result = 31 * result + useVnc.hashCode()
        return result
    }

    val monitorPort: Int
    get() = id + 4444
    fun getParams(): String{
        val params = ArrayList<String>()
        params.add("qemu-system-${cpu.framework}")
        params.add(disks!!.params)
        params.add(bootFrom.params!!)
        params.add("-m ${memory.size}${memory.unit.qemuName}")
        params.add("-${cpu.params}")
        if (useVnc) params.add("-vnc :$id")
        params.add("-monitor tcp:127.0.0.1:${4444+id},server,nowait")

        var result = String()
        params.forEach { result+=("$it ${if (it.isNotEmpty()) " " else ""}") }
        return result
    }
}