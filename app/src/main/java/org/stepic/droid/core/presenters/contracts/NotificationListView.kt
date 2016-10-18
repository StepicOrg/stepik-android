package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.notifications.model.Notification

interface NotificationListView {

    fun onConnectionProblem();

    fun onNeedShowNotifications(notifications: List<Notification>)

    fun onLoading();
}
