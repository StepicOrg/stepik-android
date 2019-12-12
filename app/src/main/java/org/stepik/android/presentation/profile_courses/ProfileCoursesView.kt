package org.stepik.android.presentation.profile_courses

import org.stepik.android.model.Course

interface ProfileCoursesView {
    sealed class State {
        object Idle : State()
        object SilentLoading : State()
        object Empty : State()
        object Error : State()

        class Content(val courses: List<Course>) : State()
    }

    fun setState(state: State)
}