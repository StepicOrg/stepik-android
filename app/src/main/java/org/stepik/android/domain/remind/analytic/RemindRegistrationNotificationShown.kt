package org.stepik.android.domain.remind.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent

object RemindRegistrationNotificationShown : AnalyticEvent {
    override val name: String =
        "Remind registration notification shown"
}