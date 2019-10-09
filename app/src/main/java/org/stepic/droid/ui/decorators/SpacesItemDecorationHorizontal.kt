package org.stepic.droid.ui.decorators

import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import android.view.View

class SpacesItemDecorationHorizontal(private val space: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.right = space
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.left = space
        }
    }
}
