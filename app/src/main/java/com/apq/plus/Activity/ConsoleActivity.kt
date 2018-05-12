package com.apq.plus.Activity

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.support.design.widget.FloatingActionButton
import android.view.MenuItem
import android.widget.TextView
import com.apq.plus.Env
import com.apq.plus.R
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class ConsoleActivity : AppCompatActivity() {

    private var port: Int = 4444
    lateinit var content: TextView
    lateinit var input: TextView
    lateinit var send: FloatingActionButton

    var client: Socket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_console)
        initToolbar()
        port = intent.getIntExtra("port",4444)
        content = findViewById(R.id.content)
        input = findViewById(R.id.edit)
        send = findViewById(R.id.send)
        connect()

        send.setOnClickListener {
            send(input.text.toString())
            input.text = ""
        }
    }

    private fun connect(){
        try {
            Env.switchNetThread()
            client = Socket("127.0.0.1",port)
            val bufferedReader = BufferedReader(InputStreamReader(client.also { if (it == null || it.isClosed) throw Exception("Could not Get Input Stream.") }?.getInputStream()))
            Thread({
                if (client!!.isClosed)
                    return@Thread
                var readLine = ""
                val sb = StringBuilder()
                try {
                    while (client != null && !client!!.isClosed) {
                        readLine = bufferedReader.readLine()
                        sb.append('>')
                        sb.append(readLine)
                        sb.append('\n')
                        runOnUiThread {
                            content.text = sb.toString()
                        }
                        if (client!!.isClosed)
                            return@Thread
                    }
                }catch (e: Exception){
                    e.printStackTrace()
                }
                runOnUiThread {
                    onBackPressed()
                }
                return@Thread
            }).start()
        }catch (e: Exception){
            e.printStackTrace()
            Env.makeErrorDialog(this,e.toString(),true)
        }
    }

    private fun send(what: String){
        if (client == null)
            connect()
        try {
            val pw = PrintWriter(client!!.getOutputStream())
            pw.println(what)
            pw.flush()
        }catch (e: Exception){
            e.printStackTrace()
            Env.makeErrorDialog(this,e.toString(),true)
        }
    }

    @SuppressLint("RestrictedApi")
    private fun initToolbar(){
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setTitle(R.string.base_activity_console)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId){
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return true
    }

    override fun onBackPressed() {
        if (client != null && !client!!.isClosed)
            client?.close()
        super.onBackPressed()
    }
}
