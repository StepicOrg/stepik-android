package org.stepik.android.domain.onboarding.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent

object OnboardingCompletedAnalyticEvent : AnalyticEvent {
    override val name: String =
        "Onboarding completed"
}