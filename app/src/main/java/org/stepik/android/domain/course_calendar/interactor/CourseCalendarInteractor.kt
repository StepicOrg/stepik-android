package org.stepik.android.domain.course_calendar.interactor

import android.content.Context
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import org.stepic.droid.R
import org.stepic.droid.util.doCompletableOnSuccess
import org.stepic.droid.util.mapToLongArray
import org.stepik.android.domain.calendar.model.CalendarEventData
import org.stepik.android.domain.calendar.model.CalendarItem
import org.stepik.android.domain.calendar.repository.CalendarRepository
import org.stepik.android.domain.course_calendar.model.SectionDateEvent
import org.stepik.android.domain.course_calendar.repository.CourseCalendarRepository
import org.stepik.android.view.course_content.model.CourseContentItem
import org.stepik.android.view.course_content.model.CourseContentSectionDate
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

    fun exportScheduleToCalendar(courseContentItems: List<CourseContentItem>, calendarItem: CalendarItem): Completable =
        Single
            .fromCallable {
                courseContentItems
                    .filterIsInstance<CourseContentItem.SectionItem>()
            }
            .doCompletableOnSuccess(::removeOldSchedule)
            .flatMapObservable { sectionItems ->
                sectionItems
                    .flatMap { sectionItem ->
                        sectionItem.dates.map { date -> mapDateToCalendarEventData(sectionItem, date)}
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

    private fun removeOldSchedule(sectionItems: List<CourseContentItem.SectionItem>): Completable =
        courseCalendarRepository
            .getSectionDateEventsByIds(*sectionItems.mapToLongArray { it.section.id })
            .flatMapCompletable { dateEvents ->
                calendarRepository
                    .removeCalendarEventDataByIds(*dateEvents.mapToLongArray(SectionDateEvent::eventId)) // mapToLongArray for varargs
                    .andThen(courseCalendarRepository
                        .removeSectionDateEventsByIds(*dateEvents.mapToLongArray(SectionDateEvent::sectionId)))
            }

    private fun mapDateToCalendarEventData(
        sectionItem: CourseContentItem.SectionItem,
        date: CourseContentSectionDate
    ): Pair<Long, CalendarEventData> =
        sectionItem.section.id to
                CalendarEventData(
                    title = context
                        .getString(
                            R.string.course_content_calendar_title,
                            sectionItem.section.title,
                            context.getString(date.titleRes)
                        ),
                    date = date.date
                )

}