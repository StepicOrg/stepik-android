package org.stepik.android.presentation.comment

import org.stepik.android.domain.comment.model.CommentsData

interface ComposeCommentView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object NetworkError : State()
        class Complete(val commentsData: CommentsData, val isCommentCreated: Boolean) : State()
    }

    fun setState(state: State)
    fun showNetworkError()
}