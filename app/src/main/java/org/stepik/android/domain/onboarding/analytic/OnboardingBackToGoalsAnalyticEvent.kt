package org.stepik.android.domain.onboarding.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent

object OnboardingBackToGoalsAnalyticEvent : AnalyticEvent {
    override val name: String =
        "Onboarding back to goals clicked"
}