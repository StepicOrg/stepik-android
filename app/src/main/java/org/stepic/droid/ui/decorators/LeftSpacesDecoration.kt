package org.stepic.droid.ui.decorators

import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import android.view.View

class LeftSpacesDecoration(private val spacePx: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.left = spacePx
    }
}
