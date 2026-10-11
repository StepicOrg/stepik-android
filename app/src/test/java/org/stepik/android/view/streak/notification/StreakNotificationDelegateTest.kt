package org.stepik.android.view.streak.notification

import org.junit.Assert.assertEquals
import org.junit.Test
import org.stepik.android.view.streak.model.StreakNotificationType

class StreakNotificationDelegateTest {
    @Test
    fun `recent strike less than or equal to zero selects zero streak notification`() {
        assertEquals(
            StreakNotificationType.ZERO,
            getStreakNotificationType(recentStrike = 0, solvedToday = 0)
        )
        assertEquals(
            StreakNotificationType.ZERO,
            getStreakNotificationType(recentStrike = -1, solvedToday = 1)
        )
    }

    @Test
    fun `positive recent strike above pins limit with solved today selects improvement notification`() {
        assertEquals(
            StreakNotificationType.SOLVED_TODAY,
            getStreakNotificationType(recentStrike = 381, solvedToday = 1)
        )
    }

    @Test
    fun `positive recent strike above pins limit without solved today selects call to action notification`() {
        assertEquals(
            StreakNotificationType.NOT_SOLVED_TODAY,
            getStreakNotificationType(recentStrike = 380, solvedToday = 0)
        )
    }
}
