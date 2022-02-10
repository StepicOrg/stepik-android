package org.stepik.android.view.course_info.ui.adapter.decorators

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import org.stepik.android.view.course_info.model.CourseInfoItem
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter

class CourseInfoDividerDecorator(
    @ColorInt separatorColor: Int,
    private val separatorSize: SeparatorSize
) : RecyclerView.ItemDecoration() {
    private val paint =
        Paint().apply {
            style = Paint.Style.STROKE
            color = separatorColor
        }

    private val emptySeparatorBounds = SeparatorSize(0)

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val separatorSize = getItemSeparatorSize(view, parent).size
        if (separatorSize == 0) {
            outRect.setEmpty()
        } else {
            outRect.top += getItemSeparatorSize(view, parent).size
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        for (view in parent.children) {
            val separatorSize = getItemSeparatorSize(view, parent)
            if (separatorSize.size > 0) {
                paint.strokeWidth = separatorSize.size.toFloat()
                val bottom = view.bottom - separatorSize.size / 2f
                c.drawLine(view.left.toFloat(), bottom, view.right.toFloat(), bottom, paint)
            }
        }
    }

    private fun getItemSeparatorSize(view: View, parent: RecyclerView): SeparatorSize {
        val position = (view.layoutParams as? RecyclerView.LayoutParams)
            ?.viewLayoutPosition
            ?.takeIf { it > -1 }
            ?: return emptySeparatorBounds

        val adapter = (parent.adapter as? DefaultDelegateAdapter<CourseInfoItem>)
            ?: return emptySeparatorBounds

        val item = adapter.items.getOrNull(position)
            ?: return emptySeparatorBounds

        return if (isItem(item)) {
            separatorSize
        } else {
            emptySeparatorBounds
        }
    }

    private fun isItem(item: CourseInfoItem) =
        item is CourseInfoItem.SummaryBlock ||
            item is CourseInfoItem.AboutBlock ||
            item is CourseInfoItem.Skills ||
            item is CourseInfoItem.AuthorsBlock

    class SeparatorSize(val size: Int)
}