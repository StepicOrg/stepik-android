package org.stepic.droid.notifications

import javax.inject.Inject

class RescheduleCheckerImpl
@Inject constructor(
        private var startHour: Int,
        private var endHour: Int)
    : RescheduleChecker {

    private val invertAnswer: Boolean

    init {
        if (endHour >= startHour) {
            invertAnswer = false
        } else {
            startHour = startHour.xor(endHour)
            endHour = startHour.xor(endHour)
            startHour = startHour.xor(endHour)
            invertAnswer = true
        }
    }

    override fun isRescheduleNeed(nowMillis: Long): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
