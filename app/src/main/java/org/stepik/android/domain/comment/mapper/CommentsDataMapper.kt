package org.stepik.android.domain.comment.mapper

import org.stepik.android.domain.comment.model.CommentsData
import org.stepik.android.presentation.comment.model.CommentItem
import javax.inject.Inject

class CommentsDataMapper
@Inject
constructor() {
    fun mapToCommentDataItems(
        commentIds: LongArray,
        commentsData: CommentsData,
        currentUserId: Long,
        cachedCommentItems: List<CommentItem.Data> = emptyList()
    ): List<CommentItem.Data> =
        commentsData
            .comments
            .mapNotNull { comment ->
                val user = commentsData
                    .users
                    .find { it.id == comment.user }
                    ?: return@mapNotNull null

                val vote = commentsData
                    .votes
                    .find { it.id == comment.vote }
                    ?: return@mapNotNull null

                CommentItem.Data(
                    comment = comment,
                    user = user,
                    voteStatus = CommentItem.Data.VoteStatus.Resolved(vote),
                    isCurrentUser = comment.user == currentUserId
                )
            }
            .let { newItems ->
                val items = newItems + cachedCommentItems

                val comments = items
                    .associateBy { it.comment.id }

                commentIds
                    .flatMap { commentId ->
                        val comment = comments[commentId]

                        if (comment != null) {
                            listOf(comment) + (comment.comment.replies?.mapNotNull(comments::get) ?: emptyList())
                        } else {
                            emptyList()
                        }
                    }
            }
}