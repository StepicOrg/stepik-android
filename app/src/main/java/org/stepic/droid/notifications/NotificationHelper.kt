package org.stepic.droid.notifications

import org.stepic.droid.notifications.model.Notification
import java.util.*

object NotificationHelper {
    private val validActions = HashSet<String>(8)
    val REVIEW_TAKEN = "review_taken"
    val REPLIED = "replied"
    val COMMENTED = "commented"

    init {

        validActions.add("opened")
        validActions.add("closed")
        validActions.add("soft_deadline_approach")
        validActions.add("hard_deadline_approach")
        validActions.add(REVIEW_TAKEN)
        validActions.add(REPLIED)
        validActions.add(COMMENTED)
    }

    fun isNotificationValidByAction(notification: Notification): Boolean {
        val action = notification.action;
        return action != null && validActions.contains(action)
    }
}
