package com.apq.plus.Adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.apq.plus.R
import com.apq.plus.View.MaterialItemView
import java.io.File

/**
 * Created by zhufu on 2/4/18.
 * 自定义文件选择器
 */
class FileAdapter(var dest: File,val blocker: String = "") : RecyclerView.Adapter<FileAdapter.FileHolder>() {
    var mList = dest.listFiles()
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): FileHolder {
        val view: MaterialItemView = MaterialItemView(parent!!.context,null,-1)
        return FileHolder(view)
    }

    override fun getItemCount(): Int = mList.size+1

    override fun onBindViewHolder(holder: FileHolder?, position: Int) {
        //返回按钮逻辑
        if (position == 0){
            holder!!.view.setTitle(holder.view.context.getString(R.string.base_upper_level),'…')
            holder.subtitle = if (dest.path == blocker) holder.view.context.getString(R.string.user_unable_to_get_upper)
                                else dest.parent
            holder.view.setOnClickListener {
                if (dest.path != blocker){
                    mList = dest.parentFile.listFiles()
                }
                if (mListener != null)
                    mListener!!.onClick()

                notifyDataSetChanged()
            }
        }
        //文件逻辑
        else {
            val file = mList[position-1]
            holder!!.title = file.name
            holder.subtitle = if (file.isDirectory) holder.view.context.getString(R.string.base_folder)
                                else holder.view.context.getString(R.string.base_file)

            holder.view.setOnClickListener {
                if (file.isDirectory) {
                    dest = file
                    mList = file.listFiles()
                    notifyDataSetChanged()
                }

                if (mListener != null)
                    mListener!!.onClick()
            }
        }
    }

    class FileHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val view: MaterialItemView = itemView as MaterialItemView
        var title: String = ""
        set(value) {
            view.setTitle(value)
        }
        var subtitle: String = ""
        set(value) {
            view.setSubtitle(value)
        }
    }

    var mListener : ItemClickListener? = null
    fun setItemClickListener(listener: ItemClickListener){
        mListener = listener
    }

    public interface ItemClickListener{
        fun onClick()
    }
}