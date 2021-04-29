package org.stepik.android.domain.onboarding.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent

class OnboardingOpenedAnalyticEvent(
    screen: Int
) : AnalyticEvent {
    companion object {
        private const val PARAM_SCREEN = "screen"
    }

    override val name: String =
        "Onboarding screen opened"

    override val params: Map<String, Any> =
        mapOf(PARAM_SCREEN to screen)
}