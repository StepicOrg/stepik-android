package org.stepik.android.domain.base.analytic

import android.os.Bundle
import androidx.core.os.bundleOf

interface BundleableAnalyticEvent : AnalyticEvent {
    companion object {
        // Main bundle key
        const val BUNDLEABLE_ANALYTIC_EVENT = "bundleable_analytic_event"

        const val BUNDLEABLE_EVENT_NAME = "bundleable_event_name"
        const val BUNDLEABLE_EVENT_PARAMS = "bundleable_event_params"
    }
    fun toBundle(): Bundle =
        bundleOf(
            BUNDLEABLE_EVENT_NAME to name,
            BUNDLEABLE_EVENT_PARAMS to bundleOf(*params.map { (a, b) -> a to b }.toTypedArray())
        )
}