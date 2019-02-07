package org.stepik.android.domain.calendar.repository

import io.reactivex.Single
import org.stepic.droid.model.CalendarItem

interface CalendarRepository {
    fun getCalendarItems(): Single<List<CalendarItem>>
}