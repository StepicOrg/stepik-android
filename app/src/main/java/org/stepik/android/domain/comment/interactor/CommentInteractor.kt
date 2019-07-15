package org.stepik.android.domain.comment.interactor

import io.reactivex.Single
import org.stepik.android.domain.comment.model.CommentsData
import org.stepik.android.domain.comment.repository.CommentRepository
import javax.inject.Inject

class CommentInteractor
@Inject
constructor(
    private val commentRepository: CommentRepository
) {
    fun getComments(vararg commentIds: Long): Single<CommentsData> =
        commentRepository
            .getComments(*commentIds)
}