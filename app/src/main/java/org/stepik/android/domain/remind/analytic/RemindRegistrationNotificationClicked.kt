package org.stepik.android.domain.remind.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent

object RemindRegistrationNotificationClicked : AnalyticEvent {
    override val name: String =
        "Remind registration notification clicked"
}