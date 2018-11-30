package org.stepik.android.presentation.course

import org.stepik.android.domain.course.model.CourseHeaderData
import org.stepik.android.domain.last_step.model.LastStep

interface CourseView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object NetworkError : State()
        object EmptyCourse : State()

        class CourseLoaded(val courseHeaderData: CourseHeaderData) : State()
    }

    fun setState(state: State)

    fun showEnrollmentError()
    fun showContinueLearningError()

    fun openStep(lastStep: LastStep)
}
