package com.apq.plus

import android.util.Log
import eu.darken.rxshell.cmd.Cmd
import eu.darken.rxshell.cmd.RxCmdShell
import java.io.IOException
import java.io.PrintWriter
import java.net.Socket
import com.apq.plus.Utils.VMCompat
import java.io.File


class VMObject(var baseInfo: VMCompat.BaseInfo) {
    var session: RxCmdShell.Session? = null

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
        }catch (e: Exception){
            false
        }
    }

    fun updateBaseInfo(){
        if (baseInfo.file == null)
            return
        val file = File(baseInfo.file)
        baseInfo = VMCompat.getBaseInfo(file.readText(),file)
    }

    init {
        updateRunningStatus()
    }

    fun run(onStarted: (() -> Unit)?,onDone: ((result: Cmd.Result?,e: IOException?) -> Unit)?){
        if (baseInfo.isNull)
            return
        isRunning = true
        val params = baseInfo.profile
        if (params == null){
            mExceptionListener?.invoke(Exception("Object Not Found"))
            return
        }
        if (session == null || !session!!.isAlive.blockingGet()){
            session = buildSession()
        }
        Log.d("APQ","Use directory ${Env.APQDir}")
        Thread({
            if (!params.useVnc){
                val r = session!!.submit(Cmd.builder("export DISPLAY=:${params.id}","export PLUSE_SERVER=tcp:127.0.0.1:4712").build()).blockingGet()
                Log.i("XServer","Exporting process exits with code ${r.exitCode}")
            }
            session!!.submit(Cmd.builder("cd ${Env.APQDir}/bin/ && ./${params.getParams()}").build())
                    .subscribe({ result: Cmd.Result? ->
                        onDone?.invoke(result,null)
                    })
            onStarted?.invoke()

            try {
                Thread.sleep(1000)
                Env.switchNetThread()
                Socket("127.0.0.1",params.monitorPort).close()
            }catch (e: IOException){
                e.printStackTrace()
                isRunning = false
                onDone?.invoke(null,e)
                mExceptionListener?.invoke(e)
            }
        }).start()
    }

    fun stop(onDone: ((e: Exception?) -> Unit)?){
        if (baseInfo.isNull){
            onDone?.invoke(IOException("Object Not Found"))
            return
        }
        isRunning = false
        var exception: Exception? = null
        try {
            Env.switchNetThread()
            val monitorClient = Socket("127.0.0.1",baseInfo.profile.also { if (it == null) onDone?.invoke(IOException("Object Not Found")) }!!.monitorPort)

            val os = PrintWriter(monitorClient.getOutputStream())
            os.println("quit")
            os.flush()
            os.close()
            monitorClient.close()
        }catch (e: Exception){
            e.printStackTrace()
            exception = e
            mExceptionListener?.invoke(e)
        }
        onDone?.invoke(exception)

        //Log.i("VirtualMachineStopJob",if(result?.exitCode == 0) result.output.toString() else result?.errors?.toString())
    }

    private fun buildSession() = RxCmdShell.builder().root(true).open().blockingGet()

    override fun equals(other: Any?): Boolean {
        return other is VMObject && (other.baseInfo == baseInfo && other.isRunning == isRunning)
    }

    override fun hashCode(): Int {
        var result = if (baseInfo.isNull) baseInfo.hashCode() else 0
        result = 31 * result + (session?.hashCode() ?: 0)
        result = 31 * result + (mExceptionListener?.hashCode() ?: 0)
        result = 31 * result + isRunning.hashCode()
        return result
    }
}