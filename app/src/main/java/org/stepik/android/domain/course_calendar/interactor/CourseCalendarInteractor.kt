package org.stepik.android.domain.course_calendar.interactor

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.stepic.droid.model.CalendarItem
import org.stepik.android.domain.calendar.model.CalendarEventData
import org.stepik.android.domain.calendar.repository.CalendarRepository
import org.stepik.android.domain.course_calendar.model.SectionDateEvent
import org.stepik.android.domain.course_calendar.repository.CourseCalendarRepository
import org.stepik.android.view.course_content.model.CourseContentItem
import javax.inject.Inject

class CourseCalendarInteractor
@Inject
constructor(
    private val calendarRepository: CalendarRepository,
    private val courseCalendarRepository: CourseCalendarRepository
) {
    fun getCalendarItems(): Single<List<CalendarItem>> =
            calendarRepository.getCalendarItems()

    fun syncDeadlinesWithCalendar(event: CalendarEventData, calendarItem: CalendarItem): Observable<Long> {
        return calendarRepository.syncCalendarEventData(event, calendarItem)
    }

    fun applyDatesToCalendar(dates: List<CourseContentItem>, calendarItem: CalendarItem): Completable =
        Completable.complete()

    fun mapCalendarData(dates: List<CourseContentItem>, events: List<SectionDateEvent>): List<CalendarEventData> {
        val result = arrayListOf<CalendarEventData>()
        dates.forEach { item ->
            if (item is CourseContentItem.SectionItem) {
                events
                    .find { it.sectionId == item.section.id }
                    .let {
                        result.add(CalendarEventData(
                                eventId = it?.eventId ?: -1,
                                title = item.section.title!!,
                                deadLine = item.dates.first().date)
                        )
                    }
            }
        }
        return result
    }
}