package org.stepik.android.domain.remind.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent

object RemindAppNotificationShown : AnalyticEvent {
    override val name: String =
        "Remind app notification shown"
}