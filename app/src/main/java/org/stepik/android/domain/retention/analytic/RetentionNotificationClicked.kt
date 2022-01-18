package org.stepik.android.domain.retention.analytic

import kotlinx.android.parcel.Parcelize
import org.stepik.android.domain.base.analytic.ParcelableAnalyticEvent

@Parcelize
class RetentionNotificationClicked(
    val day: Int
) : ParcelableAnalyticEvent {
    companion object {
        private const val PARAM_DAY = "day"
    }

    override val name: String =
        "Retention notification clicked"

    override val params: Map<String, Any> =
        mapOf(PARAM_DAY to day)
}