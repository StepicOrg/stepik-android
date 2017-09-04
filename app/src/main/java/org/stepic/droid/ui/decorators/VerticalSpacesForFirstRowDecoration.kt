package org.stepic.droid.ui.decorators

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

class VerticalSpacesForFirstRowDecoration(private val spacePx: Int, private val rowNumber: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
        if (parent.getChildAdapterPosition(view) % rowNumber == 0) {
            outRect.top = spacePx
        }
        outRect.bottom = spacePx
    }
}
