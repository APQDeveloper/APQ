package com.apq.plus.Utils

import android.os.Bundle
import android.os.Handler
import android.os.Message
import net.lingala.zip4j.core.ZipFile
import net.lingala.zip4j.exception.ZipException
import java.io.File

/**
 * Created by zhufu on 2/4/18.
 */
object ZipUtils {

    fun extractWithProgress(file: File,dest: File,handler: Handler,check: Boolean = true,overwrite: Boolean = false){
        if (dest.exists() && overwrite)
            dest.delete()

        val zipFile = ZipFile(file)
        zipFile.setFileNameCharset("UTF-8")

        if (check && zipFile.isValidZipFile)
            sendException(handler,ZipException("Valid Zip File"))

        val progress = zipFile.progressMonitor
        try {
            Thread({
                while (true) {
                    val percent = progress.percentDone
                    //进度处理
                    val bundle = Bundle()
                    bundle.putInt(CompressStatus.Progress,percent)
                    val message = Message()
                    message.what = CompressStatus.HANDLING
                    message.data = bundle
                    handler.sendMessage(message)
                    Thread.sleep(50)
                    if (percent >= 100)
                        break
                }

            }).start()
            Thread({
                zipFile.extractAll(dest.path)
                handler.sendEmptyMessage(CompressStatus.DONE)
            }).start()
        }catch (e : Throwable){
            sendException(handler,e)
        }

    }

    private fun sendException(handler: Handler,e : Throwable){
        val bundle = Bundle()
        bundle.putString(CompressStatus.Error,e.message)
        val message = Message()
        message.what = CompressStatus.ERROR
        message.data = bundle
        handler.sendMessage(message)
    }

    object CompressStatus{
        const val Progress = "[PROGRESS]"
        const val HANDLING = 1
        const val Error = "[ERROR]"
        const val ERROR = -1
        const val DONE = 0
    }
}