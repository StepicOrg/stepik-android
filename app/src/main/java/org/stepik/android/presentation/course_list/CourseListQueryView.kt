package org.stepik.android.presentation.course_list

import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.presentation.course_continue.CourseContinueView

interface CourseListQueryView : CourseContinueView {
    sealed class State {
        object Idle : State()
        data class Data(val courseListQuery: CourseListQuery, val courseListViewState: CourseListView.State) : State()
    }

    fun setState(state: State)
    fun showNetworkError()
}