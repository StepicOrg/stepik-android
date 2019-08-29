package org.stepik.android.presentation.comment

import org.stepik.android.domain.comment.model.DiscussionOrder
import org.stepik.android.model.comments.DiscussionProxy
import org.stepik.android.presentation.comment.model.CommentItem

interface CommentsView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object NetworkError : State()

        data class DiscussionLoaded(
            val discussionProxy: DiscussionProxy,
            val discussionOrder: DiscussionOrder,
            val commentsState: CommentsState
        )
    }

    sealed class CommentsState {
        object Loading : CommentsState()
        object EmptyComments : CommentsState()

        data class Loaded(
            val commentItems: List<CommentItem>
        )
    }

    fun setState(state: State)
}