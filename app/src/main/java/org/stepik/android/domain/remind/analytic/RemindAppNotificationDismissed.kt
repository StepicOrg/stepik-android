package org.stepik.android.domain.remind.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent

object RemindAppNotificationDismissed : AnalyticEvent {
    override val name: String =
        "Remind app notification dismissed"
}