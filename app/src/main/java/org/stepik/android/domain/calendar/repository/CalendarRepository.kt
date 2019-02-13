package org.stepik.android.domain.calendar.repository

import io.reactivex.Observable
import io.reactivex.Single
import org.stepic.droid.model.CalendarItem
import org.stepik.android.domain.calendar.model.CalendarEventData

interface CalendarRepository {
    fun syncCalendarEventData(calendarEventData: CalendarEventData, calendarItem: CalendarItem): Observable<Long>
    fun getCalendarItems(): Single<List<CalendarItem>>
}