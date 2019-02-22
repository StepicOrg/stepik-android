package org.stepik.android.cache.course_calendar

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepik.android.data.course_calendar.source.CourseCalendarCacheDataSource
import org.stepik.android.domain.course_calendar.model.SectionDateEvent
import javax.inject.Inject

class CourseCalendarDataSourceImpl
@Inject
constructor(
    private val databaseFacade: DatabaseFacade
) : CourseCalendarCacheDataSource {

    override fun getSectionDateEventsByIds(ids: List<Long>): Single<List<SectionDateEvent>> =
        Single.fromCallable {
            databaseFacade.getSectionDateEvents(ids)
        }

    override fun saveSectionDateEvents(events: List<SectionDateEvent>): Completable =
        Completable.fromAction {
            databaseFacade.addSectionDateEvents(events)
        }

    override fun removeSectionDatesEventsByIds(ids: List<Long>): Completable =
        Completable.fromAction {
            databaseFacade.removeSectionDateEvents(ids)
        }
}