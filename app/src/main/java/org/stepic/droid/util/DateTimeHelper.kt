package org.stepic.droid.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateTimeHelper {

    private val isoPattern = "yyyy-MM-dd'T'HH:mm:ssZ"

    fun getPrintableOfIsoDate(dateInISOFormat: String?, pattern: String, timeZone: TimeZone): String {
        if (dateInISOFormat == null) return ""
        val date = getDateOfIso(isoPattern)

        val finalDateFormat = SimpleDateFormat(pattern, Locale.ENGLISH)
        finalDateFormat.timeZone = timeZone

        return finalDateFormat.format(date)
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


    /**
     * Transform ISO 8601 string to Calendar.
     * Helper method for handling a most common subset of ISO 8601 strings
     * (in the following format: "2008-03-01T13:00:00+01:00"). It supports
     * parsing the "Z" timezone, but many other less-used features are
     * missing.
     */
    fun toCalendar(iso8601string: String, timeZone: TimeZone = TimeZone.getTimeZone("UTC")): Calendar {
        val calendar = GregorianCalendar.getInstance()
        val date = getDateOfIso(iso8601string)
        calendar.time = date
        calendar.timeZone = timeZone
        return calendar
    }


    private fun getDateOfIso(iso8601string: String): Date {
        var s = iso8601string.replace("Z", "+00:00")
        try {
            s = s.substring(0, 22) + s.substring(23)  // to get rid of the ":"
        } catch (e: IndexOutOfBoundsException) {
            throw ParseException("Invalid length", 0)
        }
        val dateFormat = SimpleDateFormat(isoPattern, Locale.ENGLISH)
        val date = dateFormat.parse(s)
        return date
    }

}
