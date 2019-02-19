package org.stepik.android.domain.course_calendar.interactor

import android.content.Context
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import org.stepic.droid.R
import org.stepik.android.domain.calendar.model.CalendarEventData
import org.stepik.android.domain.calendar.model.CalendarItem
import org.stepik.android.domain.calendar.repository.CalendarRepository
import org.stepik.android.domain.course_calendar.model.SectionDateEvent
import org.stepik.android.domain.course_calendar.repository.CourseCalendarRepository
import org.stepik.android.view.course_content.model.CourseContentItem
import javax.inject.Inject

class CourseCalendarInteractor
@Inject
constructor(
    private val context: Context,
    private val calendarRepository: CalendarRepository,
    private val courseCalendarRepository: CourseCalendarRepository
) {
    fun getCalendarItems(): Single<List<CalendarItem>> =
        calendarRepository.getCalendarItems()

    fun exportScheduleToCalendar(scheduleDates: List<CourseContentItem>, calendarItem: CalendarItem): Completable {
        return getSectionsEvents()
                .flatMapObservable { sectionEvents ->
                    scheduleDates
                            .filterIsInstance<CourseContentItem.SectionItem>()
                            .flatMap { sectionItem ->
                                sectionItem.dates.map { date ->
                                    sectionItem.section.id to CalendarEventData(
                                        eventId = sectionEvents
                                            .find { sectionDateEvent -> sectionDateEvent.sectionId == sectionItem.section.id }
                                            ?.eventId
                                            ?: -1,
                                        title = context.getString(
                                            R.string.course_content_calendar_title,
                                            sectionItem.section.title,
                                            context.getString(date.titleRes)
                                        ),
                                        date = date.date
                                    )
                                }
                            }
                            .toObservable()
                }
                .flatMapSingle { (sectionId, eventData) ->
                    calendarRepository
                            .saveCalendarEventData(eventData, calendarItem)
                            .map { eventId ->
                                SectionDateEvent(eventId, sectionId)
                            }
                }
                .toList()
                .flatMapCompletable(courseCalendarRepository::saveSectionDateEvents)
    }

    private fun getSectionsEvents(): Single<List<SectionDateEvent>> =
        courseCalendarRepository.getSectionDateEvents()
}