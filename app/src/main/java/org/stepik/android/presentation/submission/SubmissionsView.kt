package org.stepik.android.presentation.submission

import org.stepik.android.presentation.submission.model.SubmissionItem

interface SubmissionsView {
    sealed class State {
        object Idle : State()
        object Loading : State()

        class Content(val items: List<SubmissionItem>) : State()
    }

    fun setState(state: State)
    fun showNetworkError()
}