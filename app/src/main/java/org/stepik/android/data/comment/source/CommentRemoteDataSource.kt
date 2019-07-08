package org.stepik.android.data.comment.source

import io.reactivex.Single
import org.stepik.android.domain.comment.model.CommentsData

interface CommentRemoteDataSource {
    fun getComments(vararg commentIds: Long): Single<CommentsData>
}