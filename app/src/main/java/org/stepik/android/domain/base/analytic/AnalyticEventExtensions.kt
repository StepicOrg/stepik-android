package org.stepik.android.domain.base.analytic

import android.os.Bundle
import androidx.core.os.bundleOf

const val BUNDLEABLE_ANALYTIC_EVENT = "bundleable_analytic_event"

private const val BUNDLEABLE_EVENT_NAME = "bundleable_event_name"
private const val BUNDLEABLE_EVENT_PARAMS = "bundleable_event_params"

fun AnalyticEvent.toBundle(): Bundle =
    bundleOf(
        BUNDLEABLE_EVENT_NAME to name,
        BUNDLEABLE_EVENT_PARAMS to bundleOf(*params.map { (a, b) -> a to b }.toTypedArray())
    )

fun Bundle.toAnalyticEvent(): AnalyticEvent? {
    val eventName = getString(BUNDLEABLE_EVENT_NAME)
    val eventParams = getBundle(BUNDLEABLE_EVENT_PARAMS)
    return if (eventName == null) {
        null
    } else {
        object : AnalyticEvent {
            override val name: String =
                eventName

            override val params: Map<String, Any> =
                eventParams?.let { bundle ->
                    bundle
                        .keySet()
                        .mapNotNull { key -> bundle[key]?.let { value -> key to value } }
                        .toMap()
                } ?: emptyMap()
        }
    }
}