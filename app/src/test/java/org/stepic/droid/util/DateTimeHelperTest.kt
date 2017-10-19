package org.stepic.droid.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

class DateTimeHelperTest {

    @Test
    fun isoToCalendar() {
        val expected = Calendar.getInstance()
        expected.timeZone = TimeZone.getTimeZone("UTC")
        expected.set(2017, Calendar.JANUARY, 6, 15, 59, 6)
        expected.set(Calendar.MILLISECOND, 0)
        assertEquals(2017, expected.get(Calendar.YEAR))
        assertEquals(Calendar.JANUARY, expected.get(Calendar.MONTH))
        assertEquals(6, expected.get(Calendar.DATE))
        assertEquals(15, expected.get(Calendar.HOUR_OF_DAY))
        assertEquals(59, expected.get(Calendar.MINUTE))
        assertEquals(6, expected.get(Calendar.SECOND))

        val isoFromServer = "2017-01-06T15:59:06Z"
        val calendar = DateTimeHelper.toCalendar(isoFromServer)
        assertEquals(2017, calendar.get(Calendar.YEAR))
        assertEquals(Calendar.JANUARY, calendar.get(Calendar.MONTH))
        assertEquals(6, calendar.get(Calendar.DATE))
        assertEquals(15, calendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(59, calendar.get(Calendar.MINUTE))
        assertEquals(6, calendar.get(Calendar.SECOND))

        assertEquals(expected.timeInMillis, calendar.timeInMillis)
    }

    @Test
    fun printableOfIsoDateUtc() {
        val pattern = "dd.MM.yyyy HH:mm"
        val isoFormat = "2017-01-06T15:59:06Z"

        val result = DateTimeHelper.getPrintableOfIsoDate(isoFormat, pattern, timeZone = TimeZone.getTimeZone("UTC"))

        assertEquals("06.01.2017 15:59", result)
    }

    @Test
    fun printableOfIsoDateAnotherTimezone() {
        val pattern = "dd.MM.yyyy HH:mm"
        val isoFormat = "2017-01-06T15:59:06Z"

        val result = DateTimeHelper.getPrintableOfIsoDate(isoFormat, pattern, timeZone = TimeZone.getTimeZone("Europe/Moscow")) //is it depend on summer time?

        assertEquals("06.01.2017 18:59", result)
    }
}
