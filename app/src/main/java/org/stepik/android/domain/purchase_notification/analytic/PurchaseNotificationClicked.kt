package org.stepik.android.domain.purchase_notification.analytic

import org.stepik.android.domain.base.analytic.BundleableAnalyticEvent

data class PurchaseNotificationClicked(val courseId: Long) : BundleableAnalyticEvent {
    companion object {
        private const val PARAM_COURSE = "course"
    }

    override val name: String =
        "Purchase notification clicked"

    override val params: Map<String, Any> =
        mapOf(PARAM_COURSE to courseId)
}