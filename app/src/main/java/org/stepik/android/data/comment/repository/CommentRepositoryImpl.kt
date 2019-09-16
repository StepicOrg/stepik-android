package org.stepik.android.data.comment.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.data.comment.source.CommentRemoteDataSource
import org.stepik.android.domain.comment.model.CommentsData
import org.stepik.android.domain.comment.repository.CommentRepository
import org.stepik.android.model.comments.Comment
import javax.inject.Inject

class CommentRepositoryImpl
@Inject
constructor(
    private val commentRemoteDataSource: CommentRemoteDataSource
) : CommentRepository {
    override fun getComments(vararg commentIds: Long): Single<CommentsData> =
        commentRemoteDataSource
            .getComments(*commentIds)

    override fun createComment(comment: Comment): Single<CommentsData> =
        commentRemoteDataSource
            .createComment(comment)

    override fun saveComment(comment: Comment): Single<CommentsData> =
        commentRemoteDataSource
            .saveComment(comment)

    override fun removeComment(commentId: Long): Completable =
        commentRemoteDataSource
            .removeComment(commentId)
}