package org.stepik.android.domain.analytic.interactor

import android.os.Bundle
import com.google.gson.Gson
import io.reactivex.Completable
import org.stepik.android.domain.analytic.repository.AnalyticRepository
import org.stepik.android.cache.analytic.model.AnalyticLocalEvent
import javax.inject.Inject

class AnalyticInteractor
@Inject
constructor(
    private val analyticRepository: AnalyticRepository
) {
    companion object {
        private const val TIMESTAMP_KEY = "timestamp"
        private const val BUNDLE_MAP_KEY = "mMap"
    }
    fun logEvent(eventName: String, properties: Bundle): Completable {
        val timeStamp = properties.getLong(TIMESTAMP_KEY, 0L)
        properties.remove(TIMESTAMP_KEY)
        val analyticEvent =
            AnalyticLocalEvent(
                name = eventName,
                eventData = Gson().toJsonTree(properties).asJsonObject[BUNDLE_MAP_KEY],
                eventTimestamp = timeStamp
            )
        return analyticRepository.logEvent(analyticEvent)
    }

    fun flushEvents(): Completable =
        analyticRepository.flushEvents()
}