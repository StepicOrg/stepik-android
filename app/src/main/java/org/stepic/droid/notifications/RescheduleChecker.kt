package org.stepic.droid.notifications

interface RescheduleChecker {
    fun isRescheduleNeed(nowMillis : Long): Boolean
}
