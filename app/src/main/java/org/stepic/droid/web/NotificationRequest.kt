package org.stepic.droid.web

import org.stepic.droid.notifications.model.Notification

class NotificationRequest {
    var notification: Notification

    constructor(notification: Notification) {
        this.notification = notification
    }
}
