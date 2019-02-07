package org.stepik.android.data.calendar.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.model.CalendarItem

interface CalendarCacheDataSource {
    fun insertCalendarDates(): Completable
    fun updateCalendarDates(): Completable
    fun getCalendarPrimaryItems(): Single<List<CalendarItem>>
}