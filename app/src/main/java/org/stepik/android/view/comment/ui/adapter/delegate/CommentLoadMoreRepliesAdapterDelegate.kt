package org.stepik.android.view.comment.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.stepic.droid.R
import org.stepik.android.presentation.comment.model.CommentItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class CommentLoadMoreRepliesAdapterDelegate(
    private val onItemClick: (CommentItem.LoadMoreReplies) -> Unit
) : AdapterDelegate<CommentItem, DelegateViewHolder<CommentItem>>() {
    override fun isForViewType(position: Int, data: CommentItem): Boolean =
        data is CommentItem.LoadMoreReplies

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CommentItem> =
        ViewHolder(createView(parent, R.layout.item_comment_load_more_replies))

    private inner class ViewHolder(root: View) : DelegateViewHolder<CommentItem>(root) {
        init {
            root.setOnClickListener { (itemData as? CommentItem.LoadMoreReplies)?.let(onItemClick) }
        }

        override fun onBind(data: CommentItem) {
            data as CommentItem.LoadMoreReplies
            itemView as TextView

            itemView.text = context.getString(R.string.comments_load_more_replies,
                context.resources.getQuantityString(R.plurals.replies, data.count, data.count))
        }
    }
}