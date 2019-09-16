package org.stepik.android.domain.comment.interactor

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.comment.model.CommentsData
import org.stepik.android.domain.comment.repository.CommentRepository
import org.stepik.android.model.comments.Comment
import javax.inject.Inject

class ComposeCommentInteractor
@Inject
constructor(
    private val commentRepository: CommentRepository
) {
    fun createComment(comment: Comment): Single<CommentsData> =
        commentRepository
            .createComment(comment)

    fun saveComment(comment: Comment): Single<CommentsData> =
        commentRepository
            .saveComment(comment)

    fun removeComment(commentId: Long): Completable =
        commentRepository
            .removeComment(commentId)
}