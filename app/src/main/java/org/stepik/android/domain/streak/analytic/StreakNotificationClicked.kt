package org.stepik.android.domain.streak.analytic

import org.stepik.android.domain.base.analytic.BundleableAnalyticEvent

data class StreakNotificationClicked(
    val type: String
) : BundleableAnalyticEvent {
    companion object {
        private const val PARAM_TYPE = "type"
    }

    override val name: String =
        "Streak notification clicked"

    override val params: Map<String, Any> =
        mapOf(PARAM_TYPE to type)
}