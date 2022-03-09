package org.stepik.android.domain.purchase_notification.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent

class PurchaseNotificationDismissed(courseId: Long) : AnalyticEvent {
    companion object {
        private const val PARAM_COURSE = "course"
    }

    override val name: String =
        "Purchase notification dismissed"

    override val params: Map<String, Any> =
        mapOf(PARAM_COURSE to courseId)
}