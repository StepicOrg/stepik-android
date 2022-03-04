package org.stepik.android.domain.retention.analytic

import org.stepik.android.domain.base.analytic.BundleableAnalyticEvent

data class RetentionNotificationClicked(
    val day: Int
) : BundleableAnalyticEvent {
    companion object {
        const val PARAM_DAY = "day"
    }

    override val name: String =
        "Retention notification clicked"

    override val params: Map<String, Any> =
        mapOf(PARAM_DAY to day)
}