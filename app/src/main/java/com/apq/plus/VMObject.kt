package com.apq.plus

import android.util.Log
import com.apq.plus.Utils.VMProfile
import eu.darken.rxshell.cmd.Cmd
import eu.darken.rxshell.cmd.RxCmdShell
import java.io.IOException
import java.io.PrintWriter
import java.net.Socket
import android.os.StrictMode
import io.reactivex.functions.BiConsumer
import java.net.SocketAddress


class VMObject(val profile: VMProfile,val runningId: Int) {
    val params = profile.getParams(runningId)
    var session = buildSession()
    var monitorClient: Socket? = null

    private var mOutPutChangedListener: ((result: Cmd.Result?) -> Unit)? = null
    fun setOutPutChangedListener(l: (result: Cmd.Result?) -> Unit){
        mOutPutChangedListener = l
    }

    var isRunning: Boolean = false

    fun run(){
        isRunning = true
        if (!session.isAlive.blockingGet()){
            session = buildSession()
        }
        Log.d("APQ","Use directory ${Env.APQDir}")
        Log.i("VirtualMachines","Start ${profile.name} with params: $params")
        Thread({
            session.submit(Cmd.builder("${Env.APQDir}/bin/$params").build())
                    .subscribe(BiConsumer { result: Cmd.Result?,  throwable: Throwable->
                        mOutPutChangedListener?.invoke(result)
                    })
            try {
                Thread.sleep(1000)
                StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build())
                monitorClient = Socket("127.0.0.1",profile.monitorPort)
            }catch (e: IOException){
                e.printStackTrace()
                mOutPutChangedListener?.invoke(Cmd.Result(null,1, listOf(""),listOf(e.toString())))
            }
        }).start()
    }

    fun stop(onDone: ((e: IOException?) -> Unit)?){
        isRunning = false
        if (session.isAlive.blockingGet()){
            var exception: IOException? = null
            try {
                StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build())
                if (monitorClient!!.isConnected) {
                    monitorClient = Socket("127.0.0.1",profile.monitorPort)
                }
                val os = PrintWriter(monitorClient!!.getOutputStream())
                os.println("quit")
                os.flush()
                monitorClient!!.close()
            }catch (e: IOException){
                e.printStackTrace()
                exception = e
            }
            onDone?.invoke(exception)

            //Log.i("VirtualMachineStopJob",if(result?.exitCode == 0) result.output.toString() else result?.errors?.toString())
        }
    }

    private fun buildSession() = RxCmdShell.builder().root(true).open().blockingGet()
}