package org.stepik.android.presentation.course_list

import org.stepik.android.model.UserCourse
import org.stepik.android.presentation.course_continue.CourseContinueView

interface CourseListUserView : CourseContinueView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        data class Data(val userCourses: List<UserCourse>, val courseListViewState: CourseListView.State) : State()
    }

    fun setState(state: State)
    fun showNetworkError()
}