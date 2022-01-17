package org.stepik.android.domain.streak.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent

class StreakNotificationShown(
    type: String
) : AnalyticEvent {
    companion object {
        private const val PARAM_TYPE = "type"
    }

    override val name: String =
        "Streak notification shown"

    override val params: Map<String, Any> =
        mapOf(PARAM_TYPE to type)
}