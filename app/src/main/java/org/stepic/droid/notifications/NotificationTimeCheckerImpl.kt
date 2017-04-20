package org.stepic.droid.notifications

import org.joda.time.DateTime
import javax.inject.Inject

class NotificationTimeCheckerImpl
@Inject constructor(
        private var startHour: Int,
        private var endHour: Int)
    : NotificationTimeChecker {

    private val invertAnswer: Boolean

    init {
        if (endHour < 0 || startHour < 0) {
            throw IllegalArgumentException("interval bounds cannot be negative")
        }
        if (endHour > 23 || startHour > 23) {
            throw IllegalArgumentException("interval bounds cannot be greater than 23")
        }


        if (endHour >= startHour) {
            invertAnswer = false
        } else {
            startHour = startHour.xor(endHour)
            endHour = startHour.xor(endHour)
            startHour = startHour.xor(endHour)
            invertAnswer = true
        }
    }

    override fun isNight(nowMillis: Long): Boolean {
        val now = DateTime(nowMillis)
        val nowHourInt = now.hourOfDay().get()
        val result: Boolean = nowHourInt in startHour..(endHour - 1)
        return result.xor(invertAnswer)
    }
}
