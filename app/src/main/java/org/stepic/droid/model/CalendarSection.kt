package org.stepic.droid.model

import org.stepic.droid.core.DeadlineType

data class CalendarSection(val id: Long, val eventIdHardDeadline: Long?, val eventIdSoftDeadline: Long?, val hardDeadline: String?, val softDeadline: String?) {
    fun getEventIdBasedOnType(type: DeadlineType): Long? {
        if (type == DeadlineType.softDeadline) {
            return eventIdSoftDeadline
        } else {
            return eventIdHardDeadline
        }
    }

    fun getDeadlineBasedOnType(type: DeadlineType): String? {
        if (type == DeadlineType.softDeadline) {
            return softDeadline
        } else {
            return hardDeadline
        }
    }
}