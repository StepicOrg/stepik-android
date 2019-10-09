package org.stepik.android.view.comment.ui.adapter.delegate

import androidx.core.content.ContextCompat
import android.view.ViewGroup
import android.widget.TextView
import org.stepic.droid.R
import org.stepic.droid.ui.util.setCompoundDrawables
import org.stepik.android.view.comment.model.CommentTag
import ru.nobird.android.ui.adapterdelegatessupport.AdapterDelegate
import ru.nobird.android.ui.adapterdelegatessupport.DelegateViewHolder

class CommentTagsAdapterDelegate : AdapterDelegate<CommentTag, DelegateViewHolder<CommentTag>>() {
    override fun isForViewType(position: Int, data: CommentTag): Boolean =
        true

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CommentTag> =
        ViewHolder(createView(parent, R.layout.item_comment_tag) as TextView)

    private class ViewHolder(
        private val root: TextView
    ) : DelegateViewHolder<CommentTag>(root) {
        override fun onBind(data: CommentTag) {
            root.setText(data.textRes)
            root.setTextColor(ContextCompat.getColor(context, data.textColorRes))
            root.setBackgroundResource(data.backgroundRes)
            root.setCompoundDrawables(start = data.compoundDrawableRes)
        }
    }
}