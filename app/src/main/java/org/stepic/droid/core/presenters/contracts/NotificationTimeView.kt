package org.stepic.droid.core.presenters.contracts

interface NotificationTimeView {

    fun showNotification(notificationEnabled: Boolean, notificationTimeValue: String)

    fun hideNotificationTime(changeToHide: Boolean)
}
