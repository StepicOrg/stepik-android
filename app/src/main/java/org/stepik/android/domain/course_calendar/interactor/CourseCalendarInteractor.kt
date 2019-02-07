package org.stepik.android.domain.course_calendar.interactor

import io.reactivex.Single
import org.stepic.droid.model.CalendarItem
import org.stepik.android.domain.calendar.repository.CalendarRepository
import org.stepik.android.domain.course_calendar.repository.CourseCalendarRepository
import javax.inject.Inject

class CourseCalendarInteractor
@Inject
constructor(
    private val calendarRepository: CalendarRepository,
    private val courseCalendarRepository: CourseCalendarRepository
) {
    fun getCalendarItems(): Single<List<CalendarItem>> =
            calendarRepository.getCalendarItems()
}