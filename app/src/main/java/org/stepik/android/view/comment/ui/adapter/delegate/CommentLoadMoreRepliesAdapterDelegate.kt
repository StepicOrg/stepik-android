package org.stepik.android.view.comment.ui.adapter.delegate

import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.ItemCommentLoadMoreRepliesBinding
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

    private inner class ViewHolder(containerView: android.view.View) : DelegateViewHolder<CommentItem>(containerView) {
        private val viewBinding: ItemCommentLoadMoreRepliesBinding by viewBinding { ItemCommentLoadMoreRepliesBinding.bind(itemView) }

        init {
            containerView.setOnClickListener { (itemData as? CommentItem.LoadMoreReplies)?.let(onItemClick) }
        }

        override fun onBind(data: CommentItem) {
            data as CommentItem.LoadMoreReplies

            viewBinding.commentLoadMoreText.text = context.getString(R.string.comments_load_more_replies,
                context.resources.getQuantityString(R.plurals.replies, data.count, data.count))
        }
    }
}