package org.stepic.droid.web;

import org.stepic.droid.model.Meta;
import org.stepic.droid.notifications.model.Notification;

import java.util.List;

public class NotificationResponse extends MetaResponseBase {

    List<Notification> notifications;

    public NotificationResponse(Meta meta) {
        super(meta);
    }

    public List<Notification> getNotifications() {
        return notifications;
    }
}
