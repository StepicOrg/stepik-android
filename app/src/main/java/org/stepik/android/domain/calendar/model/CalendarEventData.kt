package org.stepik.android.domain.calendar.model

import java.util.*

data class CalendarEventData(
        val eventId: Long = 0,
        val title: String,
        val deadLine: Date
)