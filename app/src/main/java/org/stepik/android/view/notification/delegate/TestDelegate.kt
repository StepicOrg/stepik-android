package org.stepik.android.view.notification.delegate

import org.stepik.android.view.notification.NotificationDelegate
import org.stepik.android.view.notification.StepikNotifManager
import javax.inject.Inject

class TestDelegate
@Inject constructor(private val stepikNotifManager: StepikNotifManager)
    : NotificationDelegate("test", stepikNotifManager) {

    companion object {
        val TEST_NOTIFICATION_ID = 10101L
    }

    override fun onNeedShowNotification() {
    }

}