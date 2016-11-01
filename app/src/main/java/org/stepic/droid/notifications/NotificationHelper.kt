package org.stepic.droid.notifications

import org.stepic.droid.notifications.model.Notification
import java.util.*

object NotificationHelper {
    private val validActions = HashSet<String>(14)
    val REVIEW_TAKEN = "review_taken"
    val REPLIED = "replied"
    val COMMENTED = "commented"
    val ISSUED_CERTIFICATE = "issued_certificate"
    val ISSUED_LICENSE = "issued_license"

    init {

        validActions.add("opened")
        validActions.add("closed")
        validActions.add("soft_deadline_approach")
        validActions.add("hard_deadline_approach")
        validActions.add(REVIEW_TAKEN)
        validActions.add(REPLIED)
        validActions.add(COMMENTED)
        validActions.add(ISSUED_CERTIFICATE)
        validActions.add(ISSUED_LICENSE)
    }

    fun isNotificationValidByAction(notification: Notification): Boolean {
        val action = notification.action;
        return action != null && validActions.contains(action)
    }
}
