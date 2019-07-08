package org.stepik.android.remote.comment

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepic.droid.web.StepicRestLoggedService
import org.stepik.android.data.comment.source.CommentRemoteDataSource
import org.stepik.android.domain.comment.model.CommentsData
import org.stepik.android.remote.comment.model.CommentResponse
import javax.inject.Inject

class CommentRemoteDataSourceImpl
@Inject
constructor(
    private val loggedService: StepicRestLoggedService
) : CommentRemoteDataSource {
    private val commentResponseMapper = Function { response: CommentResponse ->
        CommentsData(
            comments = response.comments ?: emptyList(),
            users = response.users ?: emptyList(),
            votes = response.votes ?: emptyList()
        )
    }

    override fun getComments(vararg commentIds: Long): Single<CommentsData> =
        loggedService
            .getCommentsReactive(commentIds)
            .map(commentResponseMapper)
}