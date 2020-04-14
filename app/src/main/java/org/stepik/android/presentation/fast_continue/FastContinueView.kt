package org.stepik.android.presentation.fast_continue

import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.presentation.course_continue.CourseContinueView

interface FastContinueView : CourseContinueView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object Empty : State()
        object Anonymous : State()
        class Content(val courseListItem: CourseListItem.Data) : State()
    }

    fun setState(state: State)
}