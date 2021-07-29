package org.stepik.android.domain.onboarding.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent

class OnboardingParseErrorAnalyticEvent(
    json: String,
    lastSessionTimestamp: Long
) : AnalyticEvent {
    companion object {
        private const val PARAM_JSON = "json"
        private const val PARAM_LAST_SESSION_TIMESTAMP = "last_session_timestamp"
    }

    override val name: String =
        "Onboarding parse error"

    override val params: Map<String, Any> =
        mapOf(
            PARAM_JSON to json,
            PARAM_LAST_SESSION_TIMESTAMP to lastSessionTimestamp
        )
}