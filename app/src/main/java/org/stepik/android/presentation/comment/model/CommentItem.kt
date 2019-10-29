package org.stepik.android.presentation.comment.model

import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.model.comments.Comment
import org.stepik.android.model.comments.Vote
import org.stepik.android.model.user.User
import ru.nobird.android.core.model.Identifiable

sealed class CommentItem {
    data class Data(
        val comment: Comment,
        val user: User,
        val voteStatus: VoteStatus,
        val isFocused: Boolean,
        val solution: Solution?
    ) : CommentItem(), Identifiable<Long> {
        override val id: Long =
            comment.id

        sealed class VoteStatus {
            data class Resolved(val vote: Vote) : VoteStatus()
            object Pending : VoteStatus()
        }

        data class Solution(
            val attempt: Attempt,
            val submission: Submission
        )
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