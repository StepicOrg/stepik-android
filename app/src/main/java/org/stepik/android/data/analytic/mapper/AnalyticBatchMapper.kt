package org.stepik.android.data.analytic.mapper

import com.google.gson.JsonParser
import org.stepik.android.remote.analytic.model.AnalyticBatchEvent
import org.stepik.android.cache.analytic.model.AnalyticLocalEvent
import javax.inject.Inject

class AnalyticBatchMapper
@Inject
constructor() {
    companion object {
        private const val PLATFORM_VALUE = "android"

        private const val SOURCE = "source"
        private const val DATA = "data"
    }

    private val jsonParser = JsonParser()

    fun mapLocalToBatchEvents(localEvents: List<AnalyticLocalEvent>): List<AnalyticBatchEvent> =
        localEvents.map {
            val element = jsonParser.parse(it.eventData.asString)
            AnalyticBatchEvent(
                name = it.name,
                timeStamp = it.eventTimestamp.toDouble() / 1000,
                platform = PLATFORM_VALUE,
                source = element.asJsonObject.get(SOURCE)?.asString ?: "",
                data = element.asJsonObject.get(DATA)
            )
        }
}