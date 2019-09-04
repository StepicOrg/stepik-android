package org.stepik.android.view.comment.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import org.stepic.droid.R
import org.stepik.android.presentation.comment.model.CommentItem
import ru.nobird.android.ui.adapterdelegatessupport.AdapterDelegate
import ru.nobird.android.ui.adapterdelegatessupport.DelegateViewHolder

class CommentPlaceholderAdapterDelegate : AdapterDelegate<CommentItem, DelegateViewHolder<CommentItem>>() {
    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CommentItem> =
        ViewHolder(createView(parent, R.layout.item_comment_placeholder))

    override fun isForViewType(position: Int, data: CommentItem): Boolean =
        data is CommentItem.Placeholder

    private class ViewHolder(root: View) : DelegateViewHolder<CommentItem>(root)
}