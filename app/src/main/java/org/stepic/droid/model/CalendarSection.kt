package org.stepic.droid.model

import org.stepic.droid.core.DeadlineType
import java.util.Date

data class CalendarSection(val id: Long, val eventIdHardDeadline: Long?, val eventIdSoftDeadline: Long?, val hardDeadline: Date?, val softDeadline: Date?) {
    fun getEventIdBasedOnType(type: DeadlineType): Long? =
            if (type == DeadlineType.softDeadline) {
                eventIdSoftDeadline
            } else {
                eventIdHardDeadline
            }

    fun getDeadlineBasedOnType(type: DeadlineType): Date? =
            if (type == DeadlineType.softDeadline) {
                softDeadline
            } else {
                hardDeadline
            }
}