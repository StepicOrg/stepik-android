package org.stepik.android.domain.base.analytic

import android.os.Bundle

fun Bundle.toGenericAnalyticEvent(): AnalyticEvent? {
    val eventName = getString(BundleableAnalyticEvent.BUNDLEABLE_EVENT_NAME)
    val eventParams = getBundle(BundleableAnalyticEvent.BUNDLEABLE_EVENT_PARAMS)
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