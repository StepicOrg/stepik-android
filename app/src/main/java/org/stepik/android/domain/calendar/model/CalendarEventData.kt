package org.stepik.android.domain.calendar.model

import java.util.*

data class CalendarEventData(
    val eventId: Long,
    val title: String,
    val date: Date
)