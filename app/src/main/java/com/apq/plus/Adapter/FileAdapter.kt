package com.apq.plus.Adapter

import android.app.Activity
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
abstract class FileAdapter(var dest: File,val blocker: String = "") : RecyclerView.Adapter<FileAdapter.FileHolder>() {
    var mList : ArrayList<File> = sort(dest)
    var holders = ArrayList<FileHolder>()
    var selectedItem: File? = null
    var isAnythingSelected: Boolean = false

    abstract fun sort(dest: File) : ArrayList<File>

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): FileHolder {
        val view = MaterialItemView(parent!!.context)
        return FileHolder(view)
    }

    override fun getItemCount(): Int = mList.size+1

    override fun onBindViewHolder(holder: FileHolder?, position: Int) {
        holder!!.view.background = null
        holders.add(holder)
        //返回按钮逻辑
        if (position == 0){
            holder.view.setTitle(holder.view.context.getString(R.string.base_upper_level),'…')
            holder.subtitle = if (dest.path == blocker) holder.view.context.getString(R.string.user_unable_to_get_upper)
                                else dest.parent
            holder.view.setOnClickListener {
                if (dest.path != blocker){
                    Thread({
                        dest = dest.parentFile
                        mList = sort(dest)
                        (holder.view.context as Activity).runOnUiThread {
                            notifyDataChanged()
                        }

                    }).start()
                }
                if (mOnClickListener != null)
                    mOnClickListener!!(it,null)
            }
        }
        //文件逻辑
        else {
            val file = mList[position-1]
            holder.title = file.name
            holder.subtitle = ""
            holder.view.setShapeResource(if(file.isDirectory) R.drawable.ic_folder else R.drawable.ic_file)

            if (selectedItem != null && selectedItem!! == file)
                select(position)

            holder.view.setOnClickListener {
                if (file.isDirectory) {
                    Thread({
                        dest = file
                        mList = sort(dest)
                        (holder.view.context as Activity).runOnUiThread {
                            notifyDataChanged()
                        }
                    }).start()
                }
                else if (selectedItem != null && file == selectedItem){
                    select(-1)
                }
                else {
                    select(position)
                }

                if (mOnClickListener != null)
                    mOnClickListener!!(it,file)
            }
        }
    }

    fun notifyDataChanged(){
        holders.clear()
        notifyDataSetChanged()
    }

    fun select(position: Int){
        holders.forEach {
            it.view.background = null
        }
        if (position != -1) {
            holders[position].view.background = holders[position].view.context.getDrawable(R.color.colorPrimaryLight)
            selectedItem = mList[position - 1]
            if (mSelectStateChangeListener != null)
                mSelectStateChangeListener!!(selectedItem,States.SELECTED)
        }
        else{
            selectedItem = null
            if (mSelectStateChangeListener != null)
                mSelectStateChangeListener!!(null,States.UNSELECTED)
        }
    }

    fun refresh(activity: Activity){
        Thread({
            selectedItem = null
            mSelectStateChangeListener!!(null,States.UNSELECTED)
            mList = sort(dest)
            activity.runOnUiThread {
                notifyDataChanged()
            }
        }).start()
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

    private var mOnClickListener: ((View,File?) -> Unit)? = null
    fun setItemClickListener(listener: (View,File?) -> Unit){
        mOnClickListener = listener
    }

    private var mSelectStateChangeListener : ((File?, States) -> Unit)? = null
    fun setSelectStateChangeListener(listener: (File?, States) -> Unit){
        mSelectStateChangeListener = listener

    }
    enum class States{
        SELECTED,UNSELECTED
    }
}