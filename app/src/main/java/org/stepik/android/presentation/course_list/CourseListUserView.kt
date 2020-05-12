package org.stepik.android.presentation.course_list

import org.stepik.android.domain.course_list.model.CourseListUserQuery
import org.stepik.android.domain.user_courses.model.UserCourse
import org.stepik.android.presentation.course_continue.CourseContinueView
import org.stepik.android.presentation.course_list.model.CourseListUserType

interface CourseListUserView : CourseContinueView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object NetworkError : State()
        object EmptyLogin : State()
        data class Data(
            val courseListUserType: CourseListUserType,
            val courseListUserQuery: CourseListUserQuery,
            val userCourses: List<UserCourse>,
            val courseListViewState: CourseListView.State
        ) : State()
    }

    fun setState(state: State)
    fun showNetworkError()
}