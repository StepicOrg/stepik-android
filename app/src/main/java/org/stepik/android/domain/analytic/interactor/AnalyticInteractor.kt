package org.stepik.android.domain.analytic.interactor

import android.os.Bundle
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import io.reactivex.Completable
import org.stepik.android.cache.analytic.model.AnalyticLocalEvent
import org.stepik.android.domain.analytic.repository.AnalyticRepository
import javax.inject.Inject

class AnalyticInteractor
@Inject
constructor(
    private val analyticRepository: AnalyticRepository,
    private val gson: Gson
) {
    companion object {
        private const val TIMESTAMP_KEY = "timestamp"
    }
    fun logEvent(eventName: String, bundle: Bundle): Completable {
        val properties: HashMap<String, JsonElement> = HashMap()
        bundle.keySet()?.forEach {
            properties[it] = JsonParser().parse(java.lang.String.valueOf(bundle[it]))
        }

        val timeStamp = properties.remove(TIMESTAMP_KEY)?.asLong ?: 0L
        val analyticEvent =
            AnalyticLocalEvent(
                name = eventName,
                eventData = gson.toJsonTree(properties),
                eventTimestamp = timeStamp
            )
        return analyticRepository.logEvent(analyticEvent)
    }

    fun flushEvents(): Completable =
        analyticRepository.flushEvents()
}