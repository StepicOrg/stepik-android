package org.stepik.android.domain.purchase_notification.analytic

import kotlinx.android.parcel.Parcelize
import org.stepik.android.domain.base.analytic.ParcelableAnalyticEvent

@Parcelize
class PurchaseNotificationDismissed(val courseId: Long) : ParcelableAnalyticEvent {
    companion object {
        private const val PARAM_COURSE = "course"
    }

    override val name: String =
        "Purchase notification dismissed"

    override val params: Map<String, Any> =
        mapOf(PARAM_COURSE to courseId)
}