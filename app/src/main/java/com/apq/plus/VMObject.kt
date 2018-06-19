package com.apq.plus

import eu.darken.rxshell.cmd.Cmd
import eu.darken.rxshell.cmd.RxCmdShell
import java.io.IOException
import java.io.PrintWriter
import java.net.Socket
import com.apq.plus.Utils.VMCompat
import timber.log.Timber
import java.io.File


class VMObject(val file: File) {
    var baseInfo = VMCompat.getBaseInfo(file)

    private var mExceptionListener: ((e: Exception) -> Unit)? = null
    fun setExceptionListener(l: (e: Exception) -> Unit){
        mExceptionListener = l
    }

    var isRunning: Boolean = false
    fun updateRunningStatus(){
        Env.switchNetThread()
        isRunning = try {
            val tmp = Socket("127.0.0.1",baseInfo.monitorPort)
            tmp.close()
            true
        }catch (e: Throwable){
            false
        }
    }

    fun updateBaseInfo(){
        if (baseInfo.file == null)
            return
        val file = File(baseInfo.file)
        baseInfo = VMCompat.getBaseInfo(file)
    }

    init {
        updateRunningStatus()
    }

    fun run(root: Boolean, onStarted: (() -> Unit)?,onDone: ((result: Cmd.Result?) -> Unit)?){
        if (baseInfo.isNull)
            return
        isRunning = true
        val params = baseInfo.profile
        if (params == null){
            mExceptionListener?.invoke(Exception("Object Not Found"))
            return
        }
        val session = RxCmdShell.builder().root(root).open().blockingGet()
        Timber.tag("APQ").d("Use directory ${Env.APQDir}")
        if (!params.useVnc){
            val r = session!!.submit(Cmd.builder("export DISPLAY=:${params.videoPort}","export PLUSE_SERVER=tcp:127.0.0.1:4712").build()).blockingGet()
            Timber.tag("APQ:XServer").i("Exporting process exits with code ${r.exitCode}")
        }
        session!!.submit(Cmd.builder("cd ${Env.APQDir}/bin/ && ./${params.getParams()}").build())
                .subscribe { r: Cmd.Result? ->
                    onDone?.invoke(r)
                }
        onStarted?.invoke()

        try {
            Thread.sleep(300)
            Env.switchNetThread()
            Socket("127.0.0.1",params.monitorPort).close()
        }catch (e: Exception){
            e.printStackTrace()
            isRunning = false
            onDone?.invoke(null)
        }
    }

    fun stop(onDone: (() -> Unit)?){
        if (baseInfo.isNull){
            onDone?.invoke()
            mExceptionListener?.invoke(IOException("Object Not Found"))
            return
        }
        isRunning = false
        var exception: Exception? = null
        var monitorClient: Socket? = null
        var os: PrintWriter? = null
        try {
            Env.switchNetThread()
            monitorClient = Socket("127.0.0.1",baseInfo.profile.also { if (it == null) {onDone?.invoke();mExceptionListener?.invoke(IOException("Object Not Found"))} }!!.monitorPort)

            os = PrintWriter(monitorClient.getOutputStream())
            os.println("quit")
            os.flush()
        }catch (e: Throwable){
            e.printStackTrace()
            exception = Exception(e)
            mExceptionListener?.invoke(exception)
        }finally {
            if (os != null)
                os.close()
            if (monitorClient != null)
                monitorClient.close()
        }
        onDone?.invoke()
        //Log.i("VirtualMachineStopJob",if(result?.exitCode == 0) result.output.toString() else result?.errors?.toString())
    }

    override fun equals(other: Any?): Boolean {
        return other is VMObject
                && (other.baseInfo == baseInfo && other.isRunning == isRunning)
    }

    override fun hashCode(): Int {
        var result = if (baseInfo.isNull) baseInfo.hashCode() else 0
        result = 31 * result + (mExceptionListener?.hashCode() ?: 0)
        result = 31 * result + isRunning.hashCode()
        return result
    }
}