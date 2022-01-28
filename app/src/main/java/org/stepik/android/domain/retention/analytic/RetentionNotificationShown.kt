package org.stepik.android.domain.retention.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent

class RetentionNotificationShown(
    day: Int
) : AnalyticEvent {
    companion object {
        private const val PARAM_DAY = "day"
    }

    override val name: String =
        "Retention notification shown"

    override val params: Map<String, Any> =
        mapOf(PARAM_DAY to day)
}