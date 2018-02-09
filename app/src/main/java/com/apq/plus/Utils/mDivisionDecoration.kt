package com.apq.plus.Utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import kotlin.math.roundToInt

/**
 * Created by zhufu on 2/6/18.
 * 带分割线的ItemDecoration
 */
class mDivisionDecoration(context: Context) : RecyclerView.ItemDecoration() {
    private var mDivider: Drawable = context.obtainStyledAttributes(intArrayOf(android.R.attr.listDivider)).getDrawable(0)

    override fun onDraw(c: Canvas?, parent: RecyclerView?, state: RecyclerView.State?) {
        if (!(parent!!.layoutManager is LinearLayoutManager && (parent.layoutManager as LinearLayoutManager).orientation == LinearLayoutManager.VERTICAL))
            throw IllegalAccessException("Layout Manager must be LinearLayoutManager and Orientation must be VERTICAL")
        super.onDraw(c, parent, state)
        val count = parent.childCount
        val item = parent.getChildAt(0)
        val left = item.left + 30
        val right = item.right - 30
        for (i in 0 until count){
            val child = parent.getChildAt(i)
            val bottom = child.bottom
            val top = bottom - mDivider.intrinsicHeight
            mDivider.setBounds(left,top,right, bottom)
            mDivider.draw(c!!)
        }
    }

    override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
        outRect!!.set(0,0,0,mDivider.intrinsicHeight)
    }
}