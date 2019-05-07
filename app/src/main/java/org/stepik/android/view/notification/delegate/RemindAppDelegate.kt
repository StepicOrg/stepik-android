package org.stepik.android.view.notification.delegate

import org.stepik.android.view.notification.NotificationDelegate
import org.stepik.android.view.notification.StepikNotifManager
import javax.inject.Inject

class RemindAppDelegate
@Inject constructor(private val stepikNotifManager: StepikNotifManager)
    : NotificationDelegate("show_new_user_notification", stepikNotifManager) {
    override fun onNeedShowNotification() {
    }
}