package org.stepik.android.data.calendar.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.calendar.model.CalendarEventData
import org.stepik.android.domain.calendar.model.CalendarItem

interface CalendarCacheDataSource {
    fun saveCalendarEventData(calendarEventData: CalendarEventData, calendarItem: CalendarItem): Single<Long>
    fun getCalendarPrimaryItems(): Single<List<CalendarItem>>
    fun removeEventsById(ids: List<Long>): Completable
}