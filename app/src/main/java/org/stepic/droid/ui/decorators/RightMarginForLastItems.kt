package org.stepic.droid.ui.decorators

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class RightMarginForLastItems(private val rightRecyclerPadding: Int, private val rowNumber: Int) : RecyclerView.ItemDecoration() {
    private var oldAdapterCount = -1
    private var lastIndex = -1

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val adapterPosition = parent.getChildAdapterPosition(view)
        if (adapterPosition >= getLastColumnStartIndex(parent)) {
            outRect.right = -rightRecyclerPadding
        }
    }

    /**
     * Return start index of the last column
     */
    private fun getLastColumnStartIndex(parent: RecyclerView): Int {
        val actualAdapterCount = parent.adapter?.itemCount ?: 0
        if (oldAdapterCount != actualAdapterCount) {
            //if adapter count is not actual -> make first init
            var multiplier = actualAdapterCount / rowNumber
            if (actualAdapterCount % rowNumber == 0) {
                multiplier--
            }
            oldAdapterCount = actualAdapterCount
            lastIndex = rowNumber * multiplier
        }
        return lastIndex
    }

}

