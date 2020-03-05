package org.stepik.android.presentation.course_list

import org.stepic.droid.util.PagedList
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.domain.course_list.model.CourseListQuery

interface CourseListView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object Empty : State()
        object NetworkError : State()

        data class Content(val courseListQuery: CourseListQuery, val courseListItems: PagedList<CourseListItem>) : State()
        class ContentLoading(val courseListItems: PagedList<CourseListItem>) : State()
    }

    fun setState(state: State)
    fun showNetworkError()
}