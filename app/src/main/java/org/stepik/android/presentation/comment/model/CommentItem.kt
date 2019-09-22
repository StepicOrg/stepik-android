package org.stepik.android.presentation.comment.model

import org.stepik.android.model.comments.Comment
import org.stepik.android.model.user.User
import org.stepik.android.presentation.vote.model.Votable
import org.stepik.android.presentation.vote.model.VoteStatus
import ru.nobird.android.core.model.Identifiable

sealed class CommentItem {
    data class Data(
        val comment: Comment,
        val user: User,
        override val voteStatus: VoteStatus,
        val isFocused: Boolean
    ) : CommentItem(), Identifiable<Long>, Votable<Data> {
        override val id: Long =
            comment.id

        override fun mutate(voteStatus: VoteStatus): Data =
            copy(voteStatus = voteStatus)
    }

    data class LoadMoreReplies(
        val parentComment: Comment,
        val lastCommentId: Long,
        val count: Int
    ) : CommentItem(), Identifiable<Long> {
        override val id: Long =
            parentComment.id
    }

    object Placeholder : CommentItem()

    data class ReplyPlaceholder(
        val parentComment: Comment
    ) : CommentItem(), Identifiable<Long> {
        override val id: Long =
            parentComment.id
    }

    data class RemovePlaceholder(
        override val id: Long,
        val isReply: Boolean
    ) : CommentItem(), Identifiable<Long>
}