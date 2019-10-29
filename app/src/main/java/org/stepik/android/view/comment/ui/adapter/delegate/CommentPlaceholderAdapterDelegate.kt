package org.stepik.android.view.comment.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import org.stepic.droid.R
import org.stepik.android.presentation.comment.model.CommentItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class CommentPlaceholderAdapterDelegate : AdapterDelegate<CommentItem, DelegateViewHolder<CommentItem>>() {
    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CommentItem> =
        ViewHolder(createView(parent, R.layout.item_comment_placeholder))

    override fun isForViewType(position: Int, data: CommentItem): Boolean =
        data is CommentItem.Placeholder ||
        data is CommentItem.ReplyPlaceholder ||
        data is CommentItem.RemovePlaceholder

    private class ViewHolder(root: View) : DelegateViewHolder<CommentItem>(root) {
        private val replyOffset =
            context.resources.getDimensionPixelOffset(R.dimen.comment_item_reply_offset)

        override fun onBind(data: CommentItem) {
            itemView.layoutParams =
                (itemView.layoutParams as ViewGroup.MarginLayoutParams).apply {
                    leftMargin =
                        if (data is CommentItem.ReplyPlaceholder ||
                            data is CommentItem.RemovePlaceholder && data.isReply
                        ) {
                            replyOffset
                        } else {
                            0
                        }
                }
        }
    }
}