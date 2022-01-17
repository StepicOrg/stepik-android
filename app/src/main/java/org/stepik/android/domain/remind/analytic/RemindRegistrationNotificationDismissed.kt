package org.stepik.android.domain.remind.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent

object RemindRegistrationNotificationDismissed : AnalyticEvent {
    override val name: String =
        "Remind registration notification dismissed"
}