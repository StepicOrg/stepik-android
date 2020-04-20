package org.stepik.android.view.comment.ui.adapter.delegate

import android.graphics.BitmapFactory
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.item_comment.view.*
import kotlinx.android.synthetic.main.layout_comment_actions.view.*
import org.stepic.droid.R
import org.stepic.droid.ui.util.setCompoundDrawables
import org.stepic.droid.ui.util.wrapWithGlide
import org.stepic.droid.util.DateTimeHelper
import org.stepik.android.model.UserRole
import org.stepik.android.model.comments.Vote
import org.stepik.android.presentation.comment.model.CommentItem
import org.stepik.android.view.base.ui.mapper.DateMapper
import org.stepik.android.view.comment.model.CommentTag
import org.stepik.android.view.submission.ui.delegate.setSubmission
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter

class CommentDataAdapterDelegate(
    private val actionListener: ActionListener
) : AdapterDelegate<CommentItem, DelegateViewHolder<CommentItem>>() {
    override fun isForViewType(position: Int, data: CommentItem): Boolean =
        data is CommentItem.Data

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CommentItem> =
        ViewHolder(createView(parent, R.layout.item_comment))

    private inner class ViewHolder(root: View) : DelegateViewHolder<CommentItem>(root), View.OnClickListener {
        private val commentUserIcon = root.commentUserIcon
        private val commentUserIconWrapper = commentUserIcon.wrapWithGlide()
        private val commentUserName = root.commentUserName

        private val commentText = root.commentText
        private val commentMenu = root.commentMenu
        private val commentTags = root.commentTags

        private val commentTime = root.commentTime
        private val commentReply = root.commentReply
        private val commentLike = root.commentLike
        private val commentDislike = root.commentDislike

        private val commentSolution = root.commentSolution

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

        private val commentTagsAdapter = DefaultDelegateAdapter<CommentTag>()

        init {
            TextViewCompat.setLineHeight(commentText.textView, context.resources.getDimensionPixelOffset(R.dimen.comment_item_text_line))

            commentReply.setOnClickListener(this)
            commentLike.setOnClickListener(this)
            commentDislike.setOnClickListener(this)
            commentMenu.setOnClickListener(this)
            commentSolution.setOnClickListener(this)

            commentUserIcon.setOnClickListener(this)
            commentUserName.setOnClickListener(this)

            commentLike.setCompoundDrawables(start = R.drawable.ic_comment_like)
            commentDislike.setCompoundDrawables(start = R.drawable.ic_comment_dislike)

            voteStatusViewStateDelegate.addState<CommentItem.Data.VoteStatus.Resolved>(commentLike, commentDislike)
            voteStatusViewStateDelegate.addState<CommentItem.Data.VoteStatus.Pending>(root.commentVoteProgress)

            commentTagsAdapter += CommentTagsAdapterDelegate()
            with(commentTags) {
                itemAnimator = null
                isNestedScrollingEnabled = false
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = commentTagsAdapter
            }
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

            commentUserIconWrapper.setImagePath(data.user.avatar ?: "", commentUserIconPlaceholder)

            commentText.setText(data.comment.text)

            commentMenu.isVisible =
                data.comment.actions?.delete == true || data.comment.actions?.edit == true

            commentTagsAdapter.items = listOfNotNull(
                CommentTag.COURSE_TEAM.takeIf { data.comment.userRole == UserRole.TEACHER || data.comment.userRole == UserRole.ASSISTANT },
                CommentTag.STAFF.takeIf { data.comment.userRole == UserRole.STAFF },
                CommentTag.PINNED.takeIf { data.comment.isPinned },
                CommentTag.MODERATOR.takeIf { data.comment.userRole == UserRole.MODERATOR }
            )
            commentTags.isVisible = commentTagsAdapter.itemCount > 0

            commentTime.text = DateMapper.mapToRelativeDate(context, DateTimeHelper.nowUtc(), data.comment.time?.time ?: 0)

            voteStatusViewStateDelegate.switchState(data.voteStatus)

            commentLike.text = data.comment.epicCount.toString()
            commentDislike.text = data.comment.abuseCount.toString()

            commentLike.isEnabled = data.comment.actions?.vote == true
            commentDislike.isEnabled = data.comment.actions?.vote == true

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

            // solution
            commentSolution.setSubmission(data.solution?.submission)
        }

        private fun showItemMenu(view: View) {
            val commentDataItem = itemData as? CommentItem.Data
                ?: return

            val popupMenu = PopupMenu(context, view)
            popupMenu.inflate(R.menu.comment_item_menu)

            popupMenu
                .menu
                .findItem(R.id.comment_item_remove)
                ?.let { menuItem ->
                    val title = SpannableString(menuItem.title)
                    title.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.new_red_color)), 0, title.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                    menuItem.title = title
                    menuItem.isVisible = commentDataItem.comment.actions?.delete == true
                }

            popupMenu
                .menu
                .findItem(R.id.comment_item_edit)
                ?.let { menuItem ->
                    menuItem.isVisible = commentDataItem.comment.actions?.edit == true
                }

            popupMenu
                .setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.comment_item_edit ->
                            actionListener.onEditCommentClicked(commentDataItem)

                        R.id.comment_item_remove ->
                            actionListener.onRemoveCommentClicked(commentDataItem)
                    }
                    true
                }

            popupMenu.show()
        }

        override fun onClick(view: View) {
            val data = itemData as? CommentItem.Data
                ?: return

            when (view.id) {
                R.id.commentReply ->
                    actionListener.onReplyClicked(data.comment.parent ?: data.id) // nested replies not supported

                R.id.commentLike ->
                    actionListener.onVoteClicked(data, Vote.Value.LIKE)

                R.id.commentDislike ->
                    actionListener.onVoteClicked(data, Vote.Value.DISLIKE)

                R.id.commentMenu ->
                    showItemMenu(view)

                R.id.commentUserIcon,
                R.id.commentUserName ->
                    actionListener.onProfileClicked(data)

                R.id.commentSolution ->
                    data.solution?.let { actionListener.onSolutionClicked(data.id, it) }
            }
        }
    }

    interface ActionListener {
        fun onProfileClicked(commentDataItem: CommentItem.Data)

        fun onReplyClicked(parentCommentId: Long)
        fun onVoteClicked(commentDataItem: CommentItem.Data, voteValue: Vote.Value)
        fun onSolutionClicked(discussionId: Long, solution: CommentItem.Data.Solution)

        fun onEditCommentClicked(commentDataItem: CommentItem.Data)
        fun onRemoveCommentClicked(commentDataItem: CommentItem.Data)
    }
}