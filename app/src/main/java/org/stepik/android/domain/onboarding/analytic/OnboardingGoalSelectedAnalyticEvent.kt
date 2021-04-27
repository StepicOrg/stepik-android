package org.stepik.android.domain.onboarding.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent

class OnboardingGoalSelectedAnalyticEvent(
    goalTitle: String
) : AnalyticEvent {
    companion object {
        private const val PARAM_GOAL = "goal"
    }
    override val name: String =
        "Onboarding goal selected"

    override val params: Map<String, Any> =
        mapOf(PARAM_GOAL to goalTitle)
}