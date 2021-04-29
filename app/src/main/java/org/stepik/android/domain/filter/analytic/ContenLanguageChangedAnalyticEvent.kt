package org.stepik.android.domain.filter.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent

class ContenLanguageChangedAnalyticEvent(
    language: String,
    source: Source
) : AnalyticEvent {
    companion object {
        private const val PARAM_LANGUAGE = "language"
        private const val PARAM_SOURCE = "source"
    }

    override val name: String =
        "Content language changed"

    override val params: Map<String, Any> =
        mapOf(
            PARAM_LANGUAGE to language,
            PARAM_SOURCE to source.value
        )

    enum class Source(val value: String) {
        CATALOG("catalog"),
        SETTINGS("settings")
    }
}