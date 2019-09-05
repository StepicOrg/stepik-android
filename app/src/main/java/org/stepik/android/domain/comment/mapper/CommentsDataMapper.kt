package org.stepik.android.domain.comment.mapper

import org.stepik.android.domain.comment.model.CommentsData
import org.stepik.android.presentation.comment.model.CommentItem
import javax.inject.Inject

class CommentsDataMapper
@Inject
constructor() {
    fun mapToCommentDataItems(commentIds: LongArray, commentsData: CommentsData): List<CommentItem.Data> =
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
                    voteStatus = CommentItem.Data.VoteStatus.Resolved(vote)
                )
            }
            .let { items ->
                commentIds
                    .flatMap { commentId ->
                        val start = items.indexOfFirst { it.comment.id == commentId }
                        val end = items.indexOfLast { it.comment.parent == commentId || it.comment.id == commentId } + 1

                        items.subList(start, end)
                    }
            }
}