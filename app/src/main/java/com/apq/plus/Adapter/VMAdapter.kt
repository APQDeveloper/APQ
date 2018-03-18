package com.apq.plus.Adapter

import android.app.Activity
import android.os.Looper
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

/**
 * Created by zhufu on 2/5/18.
 * 用于MainRecyclerView的Adapter
 * 显示已有配置文件
 */
class VMAdapter(var mList: List<VMProfile>) : RecyclerView.Adapter<VMAdapter.VMHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VMHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_vm_item,parent,false)
        return VMHolder(view)
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: VMHolder, position: Int) {
        val item = mList[position]
        holder.title.text = item.name
        if (item.icon != null)
            holder.icon.setImageBitmap(item.icon)

        holder.subtitle.setText(R.string.base_calcuating)
        Thread({
            val context = holder.subtitle.context
            var size: Double = 0.toDouble()
            item.disks!!.forEach{
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
    }

    class VMHolder(view: View) : RecyclerView.ViewHolder(view){
        val title: TextView = view.findViewById(R.id.vm_title)
        val subtitle: TextView = view.findViewById(R.id.vm_subtitle)
        val icon: AppCompatImageView = view.findViewById(R.id.vm_icon_view)
        val card: CardView = view as CardView
    }

    var mOnItemClickListener : ((item: VMProfile) -> Unit)? = null
    fun setItemOnClickListener(listener: (item: VMProfile) -> Unit){
        mOnItemClickListener = listener
    }
}