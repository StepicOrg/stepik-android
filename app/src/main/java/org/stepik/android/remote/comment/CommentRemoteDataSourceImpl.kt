package org.stepik.android.remote.comment

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepic.droid.web.StepicRestLoggedService
import org.stepik.android.data.comment.source.CommentRemoteDataSource
import org.stepik.android.domain.comment.model.CommentsData
import org.stepik.android.model.comments.Comment
import org.stepik.android.remote.comment.model.CommentRequest
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
        if (commentIds.isEmpty()) {
            Single
                .just(CommentsData(emptyList(), emptyList(), emptyList()))
        } else {
            loggedService
                .getComments(commentIds)
                .map(commentResponseMapper)
        }

    override fun createComment(comment: Comment): Single<CommentsData> =
        loggedService
            .createComment(CommentRequest(comment))
            .map(commentResponseMapper)

    override fun saveComment(comment: Comment): Single<CommentsData> =
        loggedService
            .saveComment(comment.id, CommentRequest(comment))
            .map(commentResponseMapper)

    override fun removeComment(commentId: Long): Completable =
        loggedService
            .removeComment(commentId)
}