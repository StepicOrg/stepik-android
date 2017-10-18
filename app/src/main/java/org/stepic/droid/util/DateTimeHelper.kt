package org.stepic.droid.util

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormatter

object DateTimeHelper {
    fun getPresentOfDate(dateInISOFormat: String?, formatForView: DateTimeFormatter): String {
        if (dateInISOFormat == null) return ""
        val dateTime = DateTime(dateInISOFormat)
        return formatForView.print(dateTime)
    }

    fun isNeededUpdate(timestampStored: Long, deltaInMillis: Long = AppConstants.MILLIS_IN_24HOURS): Boolean {
        //delta is 24 hours by default
        if (timestampStored == -1L) return true

        val nowTemp = DateTime.now(DateTimeZone.UTC).millis
        val delta = nowTemp - timestampStored
        return delta > deltaInMillis
    }

    fun now(): Long = DateTime.now().millis

    fun isAfterNow(yourMillis: Long): Boolean = yourMillis > now()

    fun isBeforeNow(yourMillis: Long): Boolean = yourMillis < now()

}
