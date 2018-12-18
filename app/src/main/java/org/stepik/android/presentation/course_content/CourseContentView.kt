package org.stepik.android.presentation.course_content

import org.stepic.droid.persistence.model.DownloadProgress
import org.stepik.android.model.Course
import org.stepik.android.presentation.personal_deadlines.model.PersonalDeadlinesState
import org.stepik.android.view.course_content.model.CourseContentItem

interface CourseContentView {
    sealed class State {
        object Idle : State()
        object Loading : State()

        data class CourseContentLoaded(
            val course: Course,
            val personalDeadlinesState: PersonalDeadlinesState,
            val courseContent: List<CourseContentItem>
        ) : State()
        object NetworkError : State()
    }

    fun setState(state: State)
    fun setBlockingLoading(isLoading: Boolean)

    fun updateSectionDownloadProgress(downloadProgress: DownloadProgress)
    fun updateUnitDownloadProgress(downloadProgress: DownloadProgress)

    fun showPersonalDeadlinesBanner()
    fun showPersonalDeadlinesError()
}