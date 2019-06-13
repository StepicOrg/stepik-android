package org.stepik.android.view.notification

import javax.inject.Inject

class NotificationPublisherImpl
@Inject
constructor(
    private val notificationDelegates: Set<@JvmSuppressWildcards NotificationDelegate>
) : NotificationPublisher {
    override fun onNeedShowNotificationWithId(id: String) {
        notificationDelegates
            .find { it.id == id }
            ?.onNeedShowNotification()
    }
}