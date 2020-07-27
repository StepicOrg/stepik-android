package org.stepik.android.data.analytic.mapper

import com.google.gson.JsonParser
import org.stepik.android.model.analytic.AnalyticBatchEvent
import org.stepik.android.model.analytic.AnalyticLocalEvent
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
                timeStamp = it.eventTimestamp,
                platform = PLATFORM_VALUE,
                source = element.asJsonObject.get(SOURCE)?.asString ?: "",
                data = element.asJsonObject.get(DATA)
            )
        }
}