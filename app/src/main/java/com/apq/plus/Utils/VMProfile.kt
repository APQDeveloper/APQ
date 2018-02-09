package com.apq.plus.Utils

import android.graphics.Bitmap
import com.google.gson.Gson
import java.io.File
import kotlin.collections.ArrayList

/**
 * Created by zhufu on 2/5/18.
 * 虚拟机配置文件对象
 */
class VMProfile(var name: String, var description: String, var icon: Bitmap?, var disks: DiskHolder?, var bootFrom: BootFrom,var memory: String, var net: String, var sound: String,var vga: String,var useVnc: Boolean){
    class DiskHolder {
        /**
         * @param useAs 在虚拟机运行时的标签，如硬盘标为"-hdX $FILE_NAME"，中间'X'就是其值
         * @param label Companion Object中选填，用于标记磁盘类型
         */
        class Disk(var diskFile: File?, var useAs: Char?, var label: String?) {
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
                mList.size == 1 -> if (mList[0].label == label) nextLetter(mList[0].useAs!!) else 'a'
                else -> {
                    val tmp = ArrayList<Char>()
                    mList.forEach {
                        if (it.label == label && it.useAs != null)
                            tmp.add(it.useAs!!)
                    }
                    var biggest = 0.toChar()
                    tmp.forEach { if (it>biggest) biggest = it }
                    nextLetter(biggest)
                }
            }
        }

        fun getFirst(label: String){

        }

        fun forEach(t: (Disk) -> Unit){
            mList.forEach(t)
        }
        private fun nextLetter(thisLetter: Char): Char? = if (thisLetter<'a') 'a' else if (thisLetter.plus(1)<='z') thisLetter.plus(1) else null
    }


    class BootFrom(val value: String){
        companion object {
            const val CDROM = "cdrom"
            const val HDA = "hda"
            const val NETWORK = "network"
        }
    }

    companion object {
        fun getVMProfileByJSON(profile: String): VMProfile{
            val gson = Gson()
            return gson.fromJson(profile,VMProfile::class.java)
        }
        val emptyObject = VMProfile("","",null, DiskHolder.emptyObject,BootFrom(""),"","","","",true)
    }

    override fun toString(): String {
        val gson = Gson()
        return gson.toJson(this)
    }
}