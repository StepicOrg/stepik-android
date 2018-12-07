package org.stepik.android.presentation.course_info

import org.stepik.android.domain.course_info.model.CourseInfoData


interface CourseInfoView {
    sealed class State {
        object Idle : State()
        object Loading : State()

        class CourseInfoLoaded(val courseInfoData: CourseInfoData) : State()
    }

    fun setState(state: State)
}