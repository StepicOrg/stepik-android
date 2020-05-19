package org.stepik.android.domain.base.analytic

interface AnalyticEvent {
    val name: String
    val params: Map<String, Any>
        get() = emptyMap()
}