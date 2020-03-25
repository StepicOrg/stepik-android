package org.stepik.android.view.course_list.ui.decoration

import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class HeaderDecoration(private val headerView: View) : RecyclerView.ItemDecoration() {
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        headerView.layout(parent.left, 0, parent.right, headerView.measuredHeight)
        for (i in 0 until parent.childCount) {
            val view = parent.getChildAt(i)
            if (parent.getChildAdapterPosition(view) == 0) {
                c.save()
                val height = view.measuredHeight
                val top = view.top - height
                c.translate(0f, top.toFloat())
                view.draw(c)
                c.restore()
                break
            }
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = (view.layoutParams as? RecyclerView.LayoutParams)
            ?.viewLayoutPosition
            ?.takeIf { it == 0 }
            ?: return

        outRect.set(0, headerView.height, 0, 0)
    }
}