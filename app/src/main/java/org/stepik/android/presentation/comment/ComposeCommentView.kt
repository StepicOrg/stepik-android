package org.stepik.android.presentation.comment

import org.stepik.android.domain.comment.model.CommentsData
import org.stepik.android.model.Submission

interface ComposeCommentView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object NetworkError : State()
        class Create(val submission: Submission?) : State()
        class Complete(val commentsData: CommentsData, val isCommentCreated: Boolean) : State()
    }

    fun setState(state: State)
    fun showNetworkError()
}