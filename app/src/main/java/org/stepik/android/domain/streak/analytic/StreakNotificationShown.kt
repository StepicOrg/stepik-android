package org.stepik.android.domain.streak.analytic

import kotlinx.android.parcel.Parcelize
import org.stepik.android.domain.base.analytic.ParcelableAnalyticEvent

@Parcelize
class StreakNotificationShown(
    val type: String
) : ParcelableAnalyticEvent {
    companion object {
        private const val PARAM_TYPE = "type"
    }

    override val name: String =
        "Streak notification shown"

    override val params: Map<String, Any> =
        mapOf(PARAM_TYPE to type)
}