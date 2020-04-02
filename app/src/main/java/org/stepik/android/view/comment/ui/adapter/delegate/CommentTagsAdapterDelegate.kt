package org.stepik.android.view.comment.ui.adapter.delegate

import android.content.res.ColorStateList
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import org.stepic.droid.R
import org.stepic.droid.ui.util.setCompoundDrawables
import org.stepik.android.view.comment.model.CommentTag
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class CommentTagsAdapterDelegate : AdapterDelegate<CommentTag, DelegateViewHolder<CommentTag>>() {
    override fun isForViewType(position: Int, data: CommentTag): Boolean =
        true

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CommentTag> =
        ViewHolder(createView(parent, R.layout.item_comment_tag) as TextView)

    private class ViewHolder(
        private val root: TextView
    ) : DelegateViewHolder<CommentTag>(root) {
        override fun onBind(data: CommentTag) {
            val textColor = ContextCompat.getColor(context, data.textColorRes)

            root.setText(data.textRes)
            root.setTextColor(textColor)
            root.setBackgroundResource(data.backgroundRes)
            root.setCompoundDrawables(start = data.compoundDrawableRes)
            TextViewCompat.setCompoundDrawableTintList(root, ColorStateList.valueOf(textColor))
        }
    }
}