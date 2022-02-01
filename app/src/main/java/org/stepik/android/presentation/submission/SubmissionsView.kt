package org.stepik.android.presentation.submission

import ru.nobird.app.core.model.PagedList
import org.stepik.android.domain.filter.model.SubmissionsFilterQuery
import org.stepik.android.domain.submission.model.SubmissionItem

interface SubmissionsView {
    sealed class State {
        data class Data(
            val submissionsFilterQuery: SubmissionsFilterQuery,
            val contentState: ContentState
        ) : State()
    }

    sealed class ContentState {
        object Idle : ContentState()
        object Loading : ContentState()
        object NetworkError : ContentState()
        object ContentEmpty : ContentState()

        class Content(val items: PagedList<SubmissionItem.Data>) : ContentState()
        class ContentLoading(val items: PagedList<SubmissionItem.Data>) : ContentState()
    }

    fun setState(state: State)
    fun showNetworkError()
    fun showSubmissionsFilterDialog(submissionsFilterQuery: SubmissionsFilterQuery)
}