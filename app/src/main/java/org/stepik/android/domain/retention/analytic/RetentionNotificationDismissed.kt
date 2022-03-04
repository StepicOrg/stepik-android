package org.stepik.android.domain.retention.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent

class RetentionNotificationDismissed(
    day: Int
) : AnalyticEvent {
    companion object {
        const val PARAM_DAY = "day"
    }

    override val name: String =
        "Retention notification dismissed"

    override val params: Map<String, Any> =
        mapOf(PARAM_DAY to day)
}