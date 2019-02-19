package org.stepik.android.domain.calendar.model

import java.util.*

data class CalendarEventData(
    var eventId: Long = -1,
    val title: String,
    val deadLine: Date
)