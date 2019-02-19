package org.stepik.android.domain.calendar.repository

import io.reactivex.Single
import org.stepic.droid.model.CalendarItem
import org.stepik.android.domain.calendar.model.CalendarEventData

interface CalendarRepository {
    fun saveCalendarEventData(calendarEventData: CalendarEventData, calendarItem: CalendarItem): Single<Long>
    fun getCalendarItems(): Single<List<CalendarItem>>
}