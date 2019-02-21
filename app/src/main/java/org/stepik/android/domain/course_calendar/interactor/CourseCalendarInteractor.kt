package org.stepik.android.domain.course_calendar.interactor

import android.content.Context
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import org.stepic.droid.R
import org.stepic.droid.util.doCompletableOnSuccess
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

    fun exportScheduleToCalendar(courseContentItems: List<CourseContentItem>, calendarItem: CalendarItem): Completable =
        Single.fromCallable {
            courseContentItems.filterIsInstance<CourseContentItem.SectionItem>()
        }
            .flatMap { items -> courseCalendarRepository.getSectionDateEventsByIds(items.map { it.section.id }) }
            .doCompletableOnSuccess {
                calendarRepository.deleteCalendarEventDataByIds(it.map { it.eventId })
                        .andThen(courseCalendarRepository.removeSectionDateEventsByIds(it.map { it.sectionId }))
            }
            .flatMapObservable {
                courseContentItems.filterIsInstance<CourseContentItem.SectionItem>()
                   .flatMap { sectionItem ->
                    sectionItem.dates.map { date ->
                        sectionItem.section.id to CalendarEventData(
                            title = context.getString(
                                R.string.course_content_calendar_title,
                                sectionItem.section.title,
                                context.getString(date.titleRes)
                            ),
                            date = date.date)
                    }
                }.toObservable()
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