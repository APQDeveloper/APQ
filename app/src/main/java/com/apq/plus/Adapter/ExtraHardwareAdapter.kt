package com.apq.plus.Adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.apq.plus.Utils.VMProfile
import com.apq.plus.View.MaterialItemView

/**
 * Created by zhufu on 2/13/18.
 * 额外卡片适配器
 */
class ExtraHardwareAdapter(val holder: VMProfile.HardwareHolder) : RecyclerView.Adapter<ExtraHardwareAdapter.CardHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CardHolder = CardHolder(MaterialItemView(parent!!.context))

    override fun getItemCount(): Int = holder.size

    override fun onBindViewHolder(holder: CardHolder?, position: Int) {

    }

    class CardHolder(itemView: View?) : RecyclerView.ViewHolder(itemView){
        val view = itemView as MaterialItemView
    }
}