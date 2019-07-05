package org.stepik.android.data.comment.repository

import io.reactivex.Single
import org.stepik.android.data.comment.source.CommentRemoteDataSource
import org.stepik.android.domain.comment.model.CommentsData
import org.stepik.android.domain.comment.repository.CommentRepository
import javax.inject.Inject

class CommentRepositoryImpl
@Inject
constructor(
    private val commentRemoteDataSource: CommentRemoteDataSource
) : CommentRepository {
    override fun getComments(vararg commentIds: Long): Single<CommentsData> =
        commentRemoteDataSource
            .getComments(*commentIds)
}