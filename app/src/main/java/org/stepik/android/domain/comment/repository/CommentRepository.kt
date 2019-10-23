package org.stepik.android.domain.comment.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.comment.model.CommentsData
import org.stepik.android.model.comments.Comment

interface CommentRepository {
    fun getComments(vararg commentIds: Long): Single<CommentsData>

    fun createComment(comment: Comment): Single<CommentsData>

    fun saveComment(comment: Comment): Single<CommentsData>

    fun removeComment(commentId: Long): Completable
}