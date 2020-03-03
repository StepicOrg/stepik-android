package org.stepik.android.presentation.course_list

import org.stepik.android.model.Course

interface CourseListView {
    sealed class State {
        object Idle : State()
        object Empty : State()
        object Error : State()

        class Content(val courses: List<Course>) : State()
    }

    fun setState(state: State)
}