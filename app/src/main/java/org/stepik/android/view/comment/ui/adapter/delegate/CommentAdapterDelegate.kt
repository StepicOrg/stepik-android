package org.stepik.android.view.comment.ui.adapter.delegate

import android.graphics.BitmapFactory
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_comment.view.*
import org.stepic.droid.R
import org.stepic.droid.ui.util.RoundedBitmapImageViewTarget
import org.stepic.droid.ui.util.changeVisibility
import org.stepik.android.presentation.comment.model.CommentItem
import ru.nobird.android.ui.adapterdelegatessupport.AdapterDelegate
import ru.nobird.android.ui.adapterdelegatessupport.DelegateViewHolder

class CommentAdapterDelegate : AdapterDelegate<CommentItem, DelegateViewHolder<CommentItem>>() {
    override fun isForViewType(position: Int, data: CommentItem): Boolean =
        data is CommentItem.Data

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CommentItem> =
        ViewHolder(createView(parent, R.layout.item_comment))

    private class ViewHolder(root: View) : DelegateViewHolder<CommentItem>(root) {
        private val commentUserIcon = root.commentUserIcon
        private val commentUserName = root.commentUserName

        private val commentText = root.commentText
        private val commentMenu = root.commentMenu
        private val commentTags = root.commentTags

        private val commentUserIconTarget = RoundedBitmapImageViewTarget(
            context.resources.getDimension(R.dimen.course_image_radius), commentUserIcon)

        private val commentUserIconPlaceholder = with(context.resources) {
            val coursePlaceholderBitmap = BitmapFactory.decodeResource(this, R.drawable.general_placeholder)
            val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(this, coursePlaceholderBitmap)
            circularBitmapDrawable.cornerRadius = getDimension(R.dimen.course_image_radius)
            circularBitmapDrawable
        }

        private val replyOffset =
            context.resources.getDimensionPixelOffset(R.dimen.comment_item_reply_offset)

        init {
            commentText.setTextSize(14f)
            commentText.setTextIsSelectable(true)
        }

        override fun onBind(data: CommentItem) {
            data as CommentItem.Data

            itemView.layoutParams =
                (itemView.layoutParams as ViewGroup.MarginLayoutParams).apply {
                    leftMargin =
                        if (data.comment.parent == null) {
                            0
                        } else {
                            replyOffset
                        }
                }

            commentUserName.text = data.user.fullName

            Glide.with(commentUserIcon)
                .asBitmap()
                .load(data.user.avatar)
                .placeholder(commentUserIconPlaceholder)
                .into(commentUserIconTarget)

            commentText.setText(data.comment.text)

            commentMenu.changeVisibility(false)
            commentTags.changeVisibility(false)
        }
    }
}