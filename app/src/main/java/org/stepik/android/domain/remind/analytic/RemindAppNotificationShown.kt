package org.stepik.android.domain.remind.analytic

import kotlinx.android.parcel.Parcelize
import org.stepik.android.domain.base.analytic.ParcelableAnalyticEvent

@Parcelize
object RemindAppNotificationShown : ParcelableAnalyticEvent {
    override val name: String =
        "Remind app notification shown"
}