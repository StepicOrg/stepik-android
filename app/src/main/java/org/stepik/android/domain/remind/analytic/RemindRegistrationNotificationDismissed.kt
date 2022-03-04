package org.stepik.android.domain.remind.analytic

import org.stepik.android.domain.base.analytic.BundleableAnalyticEvent

object RemindRegistrationNotificationDismissed : BundleableAnalyticEvent {
    override val name: String =
        "Remind registration notification dismissed"
}