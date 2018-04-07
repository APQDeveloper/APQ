package com.apq.plus.Adapter

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.apq.plus.R
import com.apq.plus.Utils.FileUtils
import com.apq.plus.Utils.VMProfile
import com.apq.plus.VMObject
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import java.math.BigDecimal

/**
 * Created by zhufu on 2/5/18.
 * 用于MainRecyclerView的Adapter
 * 显示已有配置文件
 */
class VMAdapter(var mList: List<VMObject>) : RecyclerView.Adapter<VMAdapter.VMHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VMHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_vm_item,parent,false)
        return VMHolder(view)
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: VMHolder, position: Int) {
        val item = mList[position]
        holder.title.text = item.profile.name
        if (item.profile.icon != null)
            holder.icon.setImageBitmap(item.profile.icon)

        holder.subtitle.setText(R.string.base_calcuating)
        Thread({
            val context = holder.subtitle.context
            var size = BigDecimal(0)
            item.profile.disks!!.forEach{
                size += FileUtils.getFileSize(it.diskFile!!,FileUtils.FileSizeUnits.MB)
            }
            (context as Activity).runOnUiThread {
                holder.subtitle.text = context.getString(R.string.base_units_mb,size.toString())
            }
        }).start()

        holder.card.setOnClickListener {
            if (mOnItemClickListener != null)
                mOnItemClickListener?.invoke(item)
        }

        val context = holder.card.context
        val pre = context.getSharedPreferences("app", Context.MODE_PRIVATE)
        if (!pre.getBoolean("isFirstVMShown",false)){
            TapTargetView.showFor(context as Activity,TapTarget.forView(holder.start,context.getString(R.string.base_instr_firstvm_t),context.getString(R.string.base_instr_firstvm_s))
                    .transparentTarget(true),object : TapTargetView.Listener() {
                override fun onTargetClick(view: TapTargetView?) {
                    super.onTargetClick(view)
                    val edit = pre.edit()
                    edit.putBoolean("isFirstVMShown",true)
                    edit.apply()
                }
            })
        }
        fun updateButtons(){
            if (item.isRunning){
                holder.stop.isEnabled = true
                holder.stop.setImageResource(R.drawable.ic_stop_black)
                holder.start.isEnabled = false
                holder.start.setImageResource(R.drawable.ic_play_arrow_grey)
            }else{
                holder.stop.isEnabled = false
                holder.stop.setImageResource(R.drawable.ic_stop_grey)
                holder.start.isEnabled = true
                holder.start.setImageResource(R.drawable.ic_play_arrow_black)
            }
        }
        holder.start.setOnClickListener {
            mStartListener?.invoke(item)
            Handler().postDelayed({
                updateButtons()
            },300)
        }
        holder.stop.setOnClickListener {
            mStopListener?.invoke(item)
            Handler().postDelayed({
                updateButtons()
            },300)
        }
        updateButtons()
    }

    class VMHolder(view: View) : RecyclerView.ViewHolder(view){
        val title: TextView = view.findViewById(R.id.vm_title)
        val subtitle: TextView = view.findViewById(R.id.vm_subtitle)
        val icon: AppCompatImageView = view.findViewById(R.id.vm_icon_view)
        val start: AppCompatImageView = view.findViewById(R.id.vm_start)
        val stop: AppCompatImageView = view.findViewById(R.id.vm_stop)
        val card: CardView = view as CardView
    }

    private var mOnItemClickListener : ((item: VMObject) -> Unit)? = null
    fun setItemOnClickListener(listener: (item: VMObject) -> Unit){
        mOnItemClickListener = listener
    }

    private var mStartListener: ((item: VMObject) -> Unit)? = null
    fun setStartListener(l: (item: VMObject) -> Unit){
        mStartListener = l
    }
    private var mStopListener: ((item: VMObject) -> Unit)? = null
    fun setStopListener(l: (item: VMObject) -> Unit){
        mStopListener = l
    }
}