package org.stepic.droid.notifications.badges

interface NotificationsBadgesListener {
    fun hideBadge()
    fun setBadge(count: Int)
}