package org.stepik.android.presentation.profile_courses

import org.stepic.droid.util.PagedList
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.presentation.course_continue.CourseContinueView

interface ProfileCoursesView : CourseContinueView {
    sealed class State {
        object Idle : State()
        object SilentLoading : State()
        object Empty : State()
        object Error : State()

        class Content(
            val courseListDataItems: PagedList<CourseListItem.Data>
        ) : State()
    }

    fun setState(state: State)
}