package org.stepic.droid.analytic

import com.google.gson.JsonObject

interface StepikAnalytic {
    fun flushEvents()
    fun logEvent(eventName: String, properties: Map<String, Any>)
}