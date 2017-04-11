package org.stepic.droid.notifications

interface NotificationTimeChecker {
    fun isNight(nowMillis : Long): Boolean
}
