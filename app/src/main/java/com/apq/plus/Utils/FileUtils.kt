package com.apq.plus.Utils

import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * File Utils
 * Created by zhufu on 2/5/18.
 */
object FileUtils {


    /**
     * Created by zhufu on 2/4/18.
     * 实现枚举子目录下的文件
     */
    private val list = ArrayList<File>()
    fun listChildFile(file: File) : List<File>{
        if (!file.exists()) return listOf()
        if (!file.isDirectory) return listOf(file)
        list.clear()
        list(file)
        return list.toList()
    }

    private fun list(dest: File){
        val tmp = dest.listFiles()
        tmp.forEach {
            if (it.isDirectory){
                list(it)
            }
            else list.add(it)
        }
    }

    /**
     * 实现递归获取文件大小
     */
    enum class FileSizeUnits{
        B,KB,MB,GB
    }
    fun getFileSize(file: File,unit: FileSizeUnits) : Double {
        var result = 0L
        if (!file.exists()){
            return result.toDouble()
        }
        if (file.isDirectory){
            listChildFile(file).forEach { result += it.readBytes().size }
        }
        else{
            result += file.readBytes().size
        }

        val b: BigDecimal
        when(unit){
            FileSizeUnits.KB -> {
                b = BigDecimal((result/1024f).toString())

            }
            FileSizeUnits.MB -> {
                b = BigDecimal((result/1024f/1024).toString())
            }
            FileSizeUnits.GB -> {
                b = BigDecimal((result/1024f/1024/1024).toString())
            }
            else -> {
                b = BigDecimal(result.toString())
            }

        }
        return b.setScale(3,RoundingMode.HALF_UP).toDouble()
    }
}