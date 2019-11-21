package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.notifications.model.Notification

interface NotificationListView {

    fun onConnectionProblem()

    fun onNeedShowNotifications(notifications: List<Notification>)

    fun onLoading()

    fun onNeedShowLoadingFooter()

    fun notCheckNotification(position: Int, notificationId: Long)

    fun markNotificationAsRead(position: Int, id: Long)

    fun onLoadingMarkingAsRead()

    fun makeEnableMarkAllButton()

    fun markAsReadSuccessfully()

    fun onConnectionProblemWhenMarkAllFail()

    fun openNotification(notification: Notification)
}
