package org.stepik.android.domain.remind.analytic

import org.stepik.android.domain.base.analytic.BundleableAnalyticEvent

object RemindAppNotificationClicked : BundleableAnalyticEvent {
    override val name: String =
        "Remind app notification clicked"
}