package org.stepik.android.presentation.course_content

import android.support.annotation.StringRes
import org.stepic.droid.persistence.model.DownloadProgress
import org.stepik.android.domain.calendar.model.CalendarItem
import org.stepik.android.model.Course
import org.stepik.android.model.Section
import org.stepik.android.model.Unit
import org.stepik.android.presentation.course_calendar.model.CalendarError
import org.stepik.android.presentation.personal_deadlines.model.PersonalDeadlinesState
import org.stepik.android.view.course_content.model.CourseContentItem

interface CourseContentView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object EmptyContent : State()

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
    fun showChangeDownloadNetworkType()
    fun showVideoQualityDialog(course: Course? = null, section: Section? = null, unit: Unit? = null)

    fun showPersonalDeadlinesBanner()
    fun showPersonalDeadlinesError()

    fun showCalendarChoiceDialog(calendarItems: List<CalendarItem>)
    fun showCalendarSyncSuccess()
    fun showCalendarError(error: CalendarError)
}