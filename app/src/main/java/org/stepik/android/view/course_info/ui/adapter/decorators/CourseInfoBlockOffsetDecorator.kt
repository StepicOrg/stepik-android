package org.stepik.android.view.course_info.ui.adapter.decorators

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

class CourseInfoBlockOffsetDecorator(
    private val offset: Int,
    private val viewTypes: IntArray
) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        val adapter = parent.adapter ?: return

        if (adapter.getItemViewType(position) !in viewTypes) return

        outRect.left += offset
        outRect.right += offset

        if (isFirstElement(position, adapter)) {
            outRect.top += offset
        }

        if (isLastElement(position, adapter)) {
            outRect.bottom += offset
        }
    }

    private fun isFirstElement(position: Int, adapter: RecyclerView.Adapter<*>): Boolean =
        position == 0 ||
                adapter.getItemViewType(position - 1) !in viewTypes

    private fun isLastElement(position: Int, adapter: RecyclerView.Adapter<*>): Boolean =
        adapter.itemCount - 1 == position ||
                adapter.getItemViewType(position + 1) !in viewTypes
}