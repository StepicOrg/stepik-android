package org.stepic.droid.core.presenters.contracts

interface NotificationTimeView {

    fun showNotificationEnabledState(notificationEnabled: Boolean, notificationTimeValue: String)

    fun hideNotificationTime(needHide: Boolean)

    fun setNewTimeInterval(timePresentationString: String)
}
