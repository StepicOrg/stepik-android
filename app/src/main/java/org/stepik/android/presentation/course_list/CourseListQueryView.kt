package org.stepik.android.presentation.course_list

import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.presentation.course_continue.CourseContinueView

interface CourseListQueryView : CourseContinueView {
    sealed class State {
        object Idle : State()
        data class Data(
            val courseListQuery: CourseListQuery,
            val courseListViewState: CourseListView.State,
            /**
             * @null is for not fetched from REMOTE with error
             */
            val sourceType: DataSourceType? = null
        ) : State()
    }

    fun setState(state: State)
    fun showNetworkError()
}