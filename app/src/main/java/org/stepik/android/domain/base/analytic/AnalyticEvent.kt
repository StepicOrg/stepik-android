package org.stepik.android.domain.base.analytic

import java.util.EnumSet

interface AnalyticEvent {
    val name: String
    val params: Map<String, Any>
        get() = emptyMap()

    val sources: EnumSet<AnalyticSource>
        get() = EnumSet.allOf(AnalyticSource::class.java)
}