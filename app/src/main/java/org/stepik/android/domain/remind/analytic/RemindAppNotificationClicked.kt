package org.stepik.android.domain.remind.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent

object RemindAppNotificationClicked : AnalyticEvent {
    override val name: String =
        "Remind app notification clicked"
}