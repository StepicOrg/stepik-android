package org.stepic.droid.ui.decorators

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

class VerticalSpacesInGridDecoration(private val spacePx: Int, private val rowNumber: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.getChildAdapterPosition(view) % rowNumber == 0) {
            //custom rules for 1st row
            //do not add any padding
        } else {
            //add padding between elements
            outRect.top = spacePx
        }
    }
}
