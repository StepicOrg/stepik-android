package org.stepik.android.view.comment.ui.adapter.delegate

import android.graphics.BitmapFactory
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_comment.view.*
import kotlinx.android.synthetic.main.layout_comment_actions.view.*
import org.stepic.droid.R
import org.stepic.droid.ui.util.RoundedBitmapImageViewTarget
import org.stepic.droid.ui.util.changeVisibility
import org.stepic.droid.ui.util.setCompoundDrawables
import org.stepic.droid.util.DateTimeHelper
import org.stepik.android.model.comments.Vote
import org.stepik.android.presentation.comment.model.CommentItem
import org.stepik.android.view.base.ui.mapper.DateMapper
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.ui.adapterdelegatessupport.AdapterDelegate
import ru.nobird.android.ui.adapterdelegatessupport.DelegateViewHolder

class CommentDataAdapterDelegate(
    private val actionListener: ActionListener
) : AdapterDelegate<CommentItem, DelegateViewHolder<CommentItem>>() {
    override fun isForViewType(position: Int, data: CommentItem): Boolean =
        data is CommentItem.Data

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CommentItem> =
        ViewHolder(createView(parent, R.layout.item_comment))

    private inner class ViewHolder(root: View) : DelegateViewHolder<CommentItem>(root), View.OnClickListener {
        private val commentUserIcon = root.commentUserIcon
        private val commentUserName = root.commentUserName

        private val commentText = root.commentText
        private val commentMenu = root.commentMenu
        private val commentTags = root.commentTags

        private val commentTime = root.commentTime
        private val commentReply = root.commentReply
        private val commentLike = root.commentLike
        private val commentDislike = root.commentDislike

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

        private val voteStatusViewStateDelegate: ViewStateDelegate<CommentItem.Data.VoteStatus> =
            ViewStateDelegate()

        init {
            commentText.setTextSize(14f)
            commentText.setTextIsSelectable(true)

            commentReply.setOnClickListener(this)
            commentLike.setOnClickListener(this)
            commentDislike.setOnClickListener(this)
            commentMenu.setOnClickListener(this)

            commentLike.setCompoundDrawables(start = R.drawable.ic_comment_like)
            commentDislike.setCompoundDrawables(start = R.drawable.ic_comment_dislike)

            voteStatusViewStateDelegate.addState<CommentItem.Data.VoteStatus.Resolved>(commentLike, commentDislike)
            voteStatusViewStateDelegate.addState<CommentItem.Data.VoteStatus.Pending>(root.commentVoteProgress)
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
            itemView.isActivated = data.isFocused

            commentUserName.text = data.user.fullName

            Glide.with(commentUserIcon)
                .asBitmap()
                .load(data.user.avatar)
                .placeholder(commentUserIconPlaceholder)
                .into(commentUserIconTarget)

            commentText.setPlainOrLaTeXTextColored(data.comment.text, R.color.new_accent_color)

            commentMenu.changeVisibility(data.isCurrentUser)
            commentTags.changeVisibility(false)

            commentTime.text = DateMapper.mapToRelativeDate(context, DateTimeHelper.nowUtc(), data.comment.time?.time ?: 0)

            voteStatusViewStateDelegate.switchState(data.voteStatus)

            commentLike.text = data.comment.epicCount.toString()
            commentDislike.text = data.comment.abuseCount.toString()

            commentLike.isEnabled = !data.isCurrentUser
            commentDislike.isEnabled = !data.isCurrentUser

            if (data.voteStatus is CommentItem.Data.VoteStatus.Resolved) {
                commentLike.alpha =
                    when (data.voteStatus.vote.value) {
                        Vote.Value.LIKE ->
                            1f
                        else ->
                            0.5f
                    }

                commentDislike.alpha =
                    when (data.voteStatus.vote.value) {
                        Vote.Value.DISLIKE ->
                            1f
                        else ->
                            0.5f
                    }
            }
        }

        override fun onClick(view: View) {
            val data = itemData as? CommentItem.Data
                ?: return

            when (view.id) {
                R.id.commentReply ->
                    actionListener.onReplyClicked(data.id)

                R.id.commentLike ->
                    actionListener.onVoteClicked(data, Vote.Value.LIKE)

                R.id.commentDislike ->
                    actionListener.onVoteClicked(data, Vote.Value.DISLIKE)
            }
        }
    }

    interface ActionListener {
        fun onReplyClicked(parentCommentId: Long)
        fun onVoteClicked(commentDataItem: CommentItem.Data, voteValue: Vote.Value)
    }
}