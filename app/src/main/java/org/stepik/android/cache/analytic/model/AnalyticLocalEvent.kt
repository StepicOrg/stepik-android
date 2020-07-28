package org.stepik.android.cache.analytic.model

import com.google.gson.JsonElement

data class AnalyticLocalEvent(
    val name: String,
    val eventData: JsonElement,
    val eventTimestamp: Long
)