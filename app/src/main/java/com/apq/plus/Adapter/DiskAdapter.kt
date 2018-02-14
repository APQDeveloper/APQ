package com.apq.plus.Adapter

import android.app.Activity
import android.content.Context
import android.support.design.widget.Snackbar
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.apq.plus.Env
import com.apq.plus.R
import com.apq.plus.Utils.VMProfile
import com.apq.plus.View.MaterialItemView

import com.apq.plus.View.TextInfo

/**
 * Created by zhufu on 2/7/18.
 * 用于VMProfileEditor中磁盘卡片的Adapter
 */
class DiskAdapter(val disk: VMProfile.DiskHolder) : RecyclerView.Adapter<DiskAdapter.DiskHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): DiskHolder = DiskHolder(MaterialItemView(parent!!.context))

    override fun getItemCount(): Int = disk.size+1

    override fun onBindViewHolder(holder: DiskHolder?, position: Int) {
        val view = holder!!.v

        val text: TextInfo
        //单个目标逻辑
        if (position != disk.size){
            val item = disk.get(position)
            val subtitle = item.diskFile!!.name
            when(item.label){
                VMProfile.DiskHolder.HardDisk -> {
                    text = TextInfo(view.context.getString(R.string.base_hard_disk,item.useAs!!.toUpperCase())
                            ,subtitle,R.drawable.ic_harddisk)
                }
                VMProfile.DiskHolder.CD -> {
                    text = TextInfo(view.context.getString(R.string.base_raw_cd_rom)
                            ,subtitle,R.drawable.ic_disk)
                }
                VMProfile.DiskHolder.FloppyDisk -> {
                    text = TextInfo(view.context.getString(R.string.base_floppy_disk,item.useAs!!.toUpperCase())
                            ,subtitle,R.drawable.ic_floppy)

                }
                else -> text = TextInfo("","")
            }

            if (position >= 1 && disk.get(position-1).label == item.label){
                view.shapeVisibility = View.INVISIBLE
            }
            else view.shapeVisibility = View.VISIBLE

            view.setOnClickListener { if (mOnItemClickListener != null) mOnItemClickListener!!(it,position) }
        }
        else {
            text = TextInfo(view.context.getString(R.string.user_add_raw),"",R.drawable.plus)

            view.setOnClickListener {
                Log.d("Popup Menu","Clicked")
                val popupMenu = PopupMenu(view.context,it)
                popupMenu.inflate(R.menu.user_add_disk_menu)
                popupMenu.show()

                popupMenu.setOnMenuItemClickListener {
                    val id = when(it.itemId){
                        R.id.hard_disk -> VMProfile.DiskHolder.HardDisk
                        R.id.cd -> VMProfile.DiskHolder.CD
                        else -> VMProfile.DiskHolder.FloppyDisk
                    }
                    if (addItemInterface != null)
                        addItemInterface!!(id)
                    true
                }
            }
        }
        view.set(text)
    }

    fun add(i: VMProfile.DiskHolder.Disk){
        val result = disk.addDisk(i)
        if (result != -1) {
            notifyItemInserted(result)
            //如果不是最后一个，则通知后面的刷新
            if (result != disk.size-1){
                for (j in result+1 until disk.size){
                    notifyItemChanged(j)
                }
            }
        }
        if (context != null) {
            val activity = context!! as Activity
            val view = (activity.window.decorView.findViewById(android.R.id.content) as ViewGroup).getChildAt(0)
            if (result == -1)
                Snackbar.make(view, R.string.user_msg_duplicate, Snackbar.LENGTH_LONG).show()

            //关闭输入法
            android.os.Handler().postDelayed({
                Env.closeSoftInput(context)
            },200)

        }
        if (mOnItemChangeListener!=null)
            mOnItemChangeListener!!.invoke()
    }
    fun change(position: Int,result: VMProfile.DiskHolder.Disk){
        //备份并删除旧目标
        val old = disk.get(position)
        disk.remove(position)
        val positionB = disk.addDisk(result)
        //如果重复
        if(positionB == -1){
            if (context == null)
                return
            val activity = context!! as Activity
            val view = (activity.window.decorView.findViewById(android.R.id.content) as ViewGroup).getChildAt(0)
            Snackbar.make(view, R.string.user_msg_duplicate, Snackbar.LENGTH_LONG).show()

            //关闭输入法
            android.os.Handler().postDelayed({
                Env.closeSoftInput(context)
            },200)
            notifyItemChanged(disk.addDisk(old))
            return
        }
        //如果useAs没有更新
        if (position == positionB) {
            notifyItemChanged(positionB)
        }
        //否则通知目标移动，如果往前移动则通知后面的刷新，否则如果向后移动就通知从原来位置到最后一个的目标刷新
        else{
            notifyItemMoved(position, positionB)
            if (positionB < position) {
                for (j in positionB until disk.size) {
                    notifyItemChanged(j)
                }
            }
            else{
                for (j in position until disk.size){
                    notifyItemChanged(j)
                }
            }
        }
        if (mOnItemChangeListener!=null)
            mOnItemChangeListener!!.invoke()
    }
    //通知移除项目后的目标刷新
    fun remove(position: Int){
        disk.remove(position)
        notifyItemRemoved(position)
        for (i in position until disk.size){
            notifyItemChanged(i)
        }

        if (mOnItemChangeListener!=null)
            mOnItemChangeListener!!.invoke()
    }

    var context: Context? = null
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
        if (recyclerView != null)
            context = recyclerView.context
    }

    private var addItemInterface : ((String) -> Unit)? = null
    fun setAddItemInterface(l: (String) -> Unit){
        addItemInterface = l
    }

    private var mOnItemClickListener: ((View,Int) -> Unit)? = null
    fun setOnItemClickListener(l: (View,Int) -> Unit){
        mOnItemClickListener = l
    }

    private var mOnItemChangeListener: (() -> Unit)? = null
    fun setOnItemChangeListener(l: () -> Unit){
        mOnItemChangeListener = l
    }

    class DiskHolder(view: View) : RecyclerView.ViewHolder(view){
        val v = view as MaterialItemView
    }
}