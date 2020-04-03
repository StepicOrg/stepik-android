package org.stepik.android.view.comment.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_comment_load_more_replies.view.*
import org.stepic.droid.R
import org.stepik.android.presentation.comment.model.CommentItem
import org.stepik.android.view.base.ui.extension.ColorExtensions
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

        private val commentLoadMoreText = root.commentLoadMoreText

        init {
            root.setOnClickListener { (itemData as? CommentItem.LoadMoreReplies)?.let(onItemClick) }
            root.setBackgroundColor(ColorExtensions.colorSurfaceWithElevationOverlay(context, context.resources.getInteger(R.integer.highlighted_element_elevation), overrideLightTheme = true))
        }

        override fun onBind(data: CommentItem) {
            data as CommentItem.LoadMoreReplies

            commentLoadMoreText.text = context.getString(R.string.comments_load_more_replies,
                context.resources.getQuantityString(R.plurals.replies, data.count, data.count))
        }
    }
}