package org.stepik.android.view.base.ui.adapter.layoutmanager

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TableLayoutManager(
    context: Context,
    private val horizontalSpanCount: Int,
    private val verticalSpanCount: Int,
    @RecyclerView.Orientation
    orientation: Int,
    reverseLayout: Boolean
) : GridLayoutManager(context, if (orientation == LinearLayoutManager.VERTICAL) horizontalSpanCount else verticalSpanCount, orientation, reverseLayout) {
    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams =
        createLayoutParams(super.generateDefaultLayoutParams())

    override fun generateLayoutParams(lp: ViewGroup.LayoutParams): RecyclerView.LayoutParams =
        createLayoutParams(super.generateLayoutParams(lp))

    override fun generateLayoutParams(c: Context?, attrs: AttributeSet?): RecyclerView.LayoutParams =
        createLayoutParams(super.generateLayoutParams(c, attrs))

    private fun createLayoutParams(lp: RecyclerView.LayoutParams): RecyclerView.LayoutParams =
        lp.also {
            if (orientation == LinearLayoutManager.VERTICAL) {
                it.height = (height - paddingTop - paddingBottom) / verticalSpanCount - lp.topMargin - lp.bottomMargin
            } else {
                it.width = (width - paddingStart - paddingEnd) / horizontalSpanCount - lp.leftMargin - lp.rightMargin
            }
        }
}
