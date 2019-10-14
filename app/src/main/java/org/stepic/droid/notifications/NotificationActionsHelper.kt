package org.stepic.droid.notifications

import org.stepic.droid.notifications.model.Notification
import java.util.HashSet

object NotificationActionsHelper {
    private val validActions = HashSet<String>(13)
    val REVIEW_TAKEN = "review_taken"
    val REPLIED = "replied"
    val COMMENTED = "commented"
    val ISSUED_CERTIFICATE = "issued_certificate"
    val ISSUED_LICENSE = "issued_license"
    val ADDED_TO_GROUP = "added_to_group"

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
        validActions.add(ADDED_TO_GROUP)
        validActions.add("finished_long_task_execution")
        validActions.add("started_long_task_execution")
        validActions.add("queued_long_task_execution")
    }

    fun isNotificationValidByAction(notification: Notification?): Boolean {
        val action = notification?.action;
        return action != null && validActions.contains(action)
    }
}
