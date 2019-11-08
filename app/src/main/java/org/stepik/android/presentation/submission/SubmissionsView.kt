package org.stepik.android.presentation.submission

import org.stepic.droid.util.PagedList
import org.stepik.android.domain.submission.model.SubmissionItem

interface SubmissionsView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object NetworkError : State()
        object ContentEmpty : State()

        class Content(val items: PagedList<SubmissionItem.Data>) : State()
        class ContentLoading(val items: PagedList<SubmissionItem.Data>) : State()
    }

    fun setState(state: State)
    fun showNetworkError()
}