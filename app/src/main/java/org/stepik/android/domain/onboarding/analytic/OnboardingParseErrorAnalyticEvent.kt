package org.stepik.android.domain.onboarding.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent

class OnboardingParseErrorAnalyticEvent(
    json: String,
    isFirstLaunch: Boolean
) : AnalyticEvent {
    companion object {
        private const val PARAM_JSON = "json"
        private const val PARAM_IS_FIRST_LAUNCH = "is_first_launch"
    }

    override val name: String =
        "Onboarding parse error"

    override val params: Map<String, Any> =
        mapOf(
            PARAM_JSON to json,
            PARAM_IS_FIRST_LAUNCH to isFirstLaunch
        )
}