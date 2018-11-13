package org.stepik.android.presentation.course

import org.stepik.android.model.Course

interface CourseView {
    sealed class State {
        object NetworkError : State()
        object EmptyCourse : State()
        object Loading : State()
        object Idle : State()

        class CourseLoaded(val course: Course) : State()
        class EnrollmentProgress(val course: Course) : State()
    }

    fun setState(state: State)

    fun showEnrollmentError()
}
