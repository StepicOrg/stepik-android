package org.stepic.droid.util

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter
import java.util.*

object DateTimeHelper {
    fun getPresentOfDate(dateInISOFormat: String?, formatForView: DateTimeFormatter): String {
        if (dateInISOFormat == null) return ""
        val dateTime = DateTime(dateInISOFormat)
        return formatForView.print(dateTime)
    }

    fun isNeededUpdate(timestampStored: Long, deltaInMillis: Long = AppConstants.MILLIS_IN_24HOURS): Boolean {
        //delta is 24 hours by default
        if (timestampStored == -1L) return true

        val nowTemp = nowUtc()
        val delta = nowTemp - timestampStored
        return delta > deltaInMillis
    }

    fun nowLocal(): Long {
        val localTimezoneCalendar = Calendar.getInstance()
        return localTimezoneCalendar.timeInMillis + localTimezoneCalendar.timeZone.rawOffset
    }

    fun nowUtc(): Long = Calendar.getInstance().timeInMillis

    fun isAfterNowUtc(yourMillis: Long): Boolean = yourMillis > nowUtc()

    fun isBeforeNowUtc(yourMillis: Long): Boolean = yourMillis < nowUtc()

}
