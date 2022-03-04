package org.stepik.android.domain.remind.analytic

import org.stepik.android.domain.base.analytic.BundleableAnalyticEvent

object RemindRegistrationNotificationClicked : BundleableAnalyticEvent {
    override val name: String =
        "Remind registration notification clicked"
}