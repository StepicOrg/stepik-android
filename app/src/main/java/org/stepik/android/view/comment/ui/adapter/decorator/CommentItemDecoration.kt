package org.stepik.android.view.comment.ui.adapter.decorator

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import androidx.core.view.children
import org.stepik.android.presentation.comment.model.CommentItem
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter

class CommentItemDecoration(
    @ColorInt separatorColor: Int,
    private val bigSeparatorBounds: SeparatorBounds,
    private val smallSeparatorBounds: SeparatorBounds
) : RecyclerView.ItemDecoration() {
    private val paint =
        Paint().apply {
            style = Paint.Style.STROKE
            color = separatorColor
        }

    private val emptySeparatorBounds =
        SeparatorBounds(0, 0)

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.top += getItemSeparatorBounds(view, parent).size
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        for (view in parent.children) {
            val separatorBounds = getItemSeparatorBounds(view, parent)
            paint.strokeWidth = separatorBounds.size.toFloat()
            val top = view.top - separatorBounds.size / 2f
            c.drawLine(separatorBounds.offset.toFloat(), top, view.right.toFloat(), top, paint)
        }
    }

    private fun getItemSeparatorBounds(view: View, parent: RecyclerView): SeparatorBounds {
        val position = (view.layoutParams as? RecyclerView.LayoutParams)
            ?.viewLayoutPosition
            ?.takeIf { it > 0 }
            ?: return emptySeparatorBounds

        val adapter = (parent.adapter as? DefaultDelegateAdapter<CommentItem>)
            ?: return emptySeparatorBounds

        val item = adapter.items.getOrNull(position)
            ?: return emptySeparatorBounds

        return when (item) {
            is CommentItem.Data ->
                if (item.comment.parent == null) {
                    bigSeparatorBounds
                } else {
                    smallSeparatorBounds
                }

            is CommentItem.LoadMoreReplies ->
                emptySeparatorBounds

            is CommentItem.Placeholder ->
                bigSeparatorBounds

            is CommentItem.ReplyPlaceholder ->
                smallSeparatorBounds

            is CommentItem.RemovePlaceholder ->
                if (item.isReply) {
                    smallSeparatorBounds
                } else {
                    bigSeparatorBounds
                }
        }
    }

    class SeparatorBounds(val size: Int, val offset: Int)
}