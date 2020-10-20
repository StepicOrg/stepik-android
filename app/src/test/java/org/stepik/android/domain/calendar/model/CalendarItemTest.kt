package org.stepik.android.domain.calendar.model

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.stepic.droid.testUtils.assertThatObjectParcelable

@RunWith(RobolectricTestRunner::class)
class CalendarItemTest {
    companion object {
        fun createTestCalendarItem(): CalendarItem =
            CalendarItem(
                calendarId = 123,
                owner = "Tester",
                isPrimary = false
            )
    }

    @Test
    fun calendarItemIsSerializable() {
        createTestCalendarItem()
            .assertThatObjectParcelable<CalendarItem>()
    }
}