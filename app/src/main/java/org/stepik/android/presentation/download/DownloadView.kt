package org.stepik.android.presentation.download

import org.stepik.android.model.Course

interface DownloadView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        class DownloadedCoursesLoaded(val courses: List<Course>) : State()
    }

    fun setState(state: State)
}