package org.stepik.android.view.course_content.ui.adapter.decorators

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

class CourseContentTimelineDecorator : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val adapterPosition = parent.getChildAdapterPosition(view)
        if (adapterPosition + 1 == parent.adapter?.itemCount) {
            outRect.right += calculateRightOffset(parent)
        }
    }

    private fun calculateRightOffset(parent: RecyclerView): Int {
        val layoutManager = parent.layoutManager ?: return 0
        val adapter = parent.adapter ?: return 0

        var offset = 0
        var position = adapter.itemCount - 1
        while (position >= 0) {
            val width = layoutManager.findViewByPosition(position)?.width ?: 0
            if (offset + width > parent.width) {
                break
            } else {
                offset += width
            }

            position--
        }

        // if we fill whole width we don't need scroll decoration
        return if (position < 0) 0 else parent.width - offset - parent.paddingLeft - parent.paddingRight
    }

}