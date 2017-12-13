package org.stepic.droid.notifications.badges

interface NotificationsBadgesListener {
    fun onBadgeShouldBeHidden()
    fun onBadgeCountChanged(count: Int)
}