package com.apq.plus.Utils

import android.util.Log
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
    fun getFileSize(file: File,unit: FileSizeUnits) : BigDecimal {
        var result = BigDecimal(0)
        if (!file.exists()){
            return result
        }
        if (file.isDirectory){
            listChildFile(file).forEach { result+= fileSize(it).toBigDecimal() }
        }
        else{
            result = fileSize(file).toBigDecimal()
        }

        val b: BigDecimal = when(unit){
            FileSizeUnits.KB -> {
                result.divide(1024.toBigDecimal())

            }
            FileSizeUnits.MB -> {
                result.divide(BigDecimal(1024).pow(2))
            }
            FileSizeUnits.GB -> {
                result.divide(BigDecimal(1024).pow(3))
            }
            else -> {
                result
            }

        }

        return b.setScale(3,RoundingMode.HALF_UP)
    }

    private fun fileSize(file: File): Long{
        var r = 0L
        if (file.isDirectory)
            return r
        val fc = file.inputStream().channel
        r = fc.size()
        Log.d("","Size of $file is $r")
        return r
    }
}