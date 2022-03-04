package org.stepik.android.domain.remind.analytic

import org.stepik.android.domain.base.analytic.BundleableAnalyticEvent

object RemindAppNotificationDismissed : BundleableAnalyticEvent {
    override val name: String =
        "Remind app notification dismissed"
}