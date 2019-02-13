package org.stepik.android.data.calendar.source

import io.reactivex.Observable
import io.reactivex.Single
import org.stepic.droid.model.CalendarItem
import org.stepik.android.domain.calendar.model.CalendarEventData

interface CalendarCacheDataSource {
    fun syncCalendarEventData(calendarEventData: CalendarEventData, calendarItem: CalendarItem): Observable<Long>
    fun getCalendarPrimaryItems(): Single<List<CalendarItem>>
}