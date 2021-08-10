package org.stepik.android.view.user_reviews.ui.adapter.decorator

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import org.stepik.android.domain.user_reviews.model.UserCourseReviewItem
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter

class UserCourseReviewItemDecoration(
    @ColorInt separatorColor: Int,
    private val defaultSeparatorSize: SeparatorSize
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

        val adapter = (parent.adapter as? DefaultDelegateAdapter<UserCourseReviewItem>)
            ?: return emptySeparatorBounds

        val item = adapter.items.getOrNull(position)
            ?: return emptySeparatorBounds

        val nextItem = adapter.items.getOrNull(position + 1)
            ?: return emptySeparatorBounds

        return if (isItem(item) && isItem(nextItem)) {
            defaultSeparatorSize
        } else {
            emptySeparatorBounds
        }
    }

    private fun isItem(item: UserCourseReviewItem) =
        item is UserCourseReviewItem.PotentialReviewItem || item is UserCourseReviewItem.ReviewedItem

    class SeparatorSize(val size: Int)
}