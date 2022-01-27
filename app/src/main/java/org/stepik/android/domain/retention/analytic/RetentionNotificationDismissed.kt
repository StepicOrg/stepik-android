package org.stepik.android.domain.retention.analytic

import kotlinx.android.parcel.Parcelize
import org.stepik.android.domain.base.analytic.ParcelableAnalyticEvent

@Parcelize
data class RetentionNotificationDismissed(
    val day: Int
) : ParcelableAnalyticEvent {
    companion object {
        private const val PARAM_DAY = "day"
    }

    override val name: String =
        "Retention notification dismissed"

    override val params: Map<String, Any> =
        mapOf(PARAM_DAY to day)
}