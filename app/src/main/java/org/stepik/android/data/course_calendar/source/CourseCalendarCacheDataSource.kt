package org.stepik.android.data.course_calendar.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.course_calendar.model.SectionDateEvent

interface CourseCalendarCacheDataSource {
    fun getSectionDateEvents(): Single<List<SectionDateEvent>>
    fun saveSectionDateEvents(events: List<SectionDateEvent>): Completable
}