package com.apq.plus.Utils

import android.os.Handler
import android.support.v7.widget.RecyclerView
import kotlin.collections.ArrayList
import kotlin.math.abs

/**
 * Created by zhufu on 3/17/18.
 * 比较并更新RecyclerView adapter的工具
 */
object Differ {
    fun <T> update(changeList: () -> Unit, old: ArrayList<T>, new: ArrayList<T>, adapter: RecyclerView.Adapter<*>){
        changeList()
        //处理不同项
        old.forEachIndexed { index, it ->
            if (!new.contains(it)){
                //如果移除数据
                adapter.notifyItemRemoved(index)
            }
        }
        new.forEachIndexed { index, it ->
            if (!old.contains(it)){
                //如果新插入数据
                adapter.notifyItemInserted(index)
            }
        }

        if (old.size == new.size) {
            //移动相同项
            new.forEachIndexed { index, it ->
                val oldPos = old.indexOf(it)
                if (oldPos != -1 && oldPos != index){
                    adapter.notifyItemMoved(oldPos,index)
                }
            }
        }
    }
}