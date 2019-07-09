package org.stepik.android.domain.comment.repository

import io.reactivex.Single
import org.stepik.android.domain.comment.model.CommentsData

interface CommentRepository {
    fun getComments(vararg commentIds: Long): Single<CommentsData>
}