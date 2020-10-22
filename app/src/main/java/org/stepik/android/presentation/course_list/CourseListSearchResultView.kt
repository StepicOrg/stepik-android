package org.stepik.android.presentation.course_list

import org.stepik.android.domain.search_result.model.SearchResultQuery
import org.stepik.android.presentation.course_continue.CourseContinueView

interface CourseListSearchResultView : CourseContinueView {
    sealed class State {
        object Idle : State()
        data class Data(
            val searchResultQuery: SearchResultQuery,
            val courseListViewState: CourseListView.State
        ) : State()
    }

    fun setState(state: State)
    fun showNetworkError()
}