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
    }
    fun logEvent(eventName: String, bundle: Bundle): Completable {
        val properties: HashMap<String, String> = HashMap()
        bundle.keySet()?.forEach {
            properties[it] = java.lang.String.valueOf(bundle[it])
        }

        val timeStamp = properties.remove(TIMESTAMP_KEY)?.toLong() ?: 0L
        val analyticEvent =
            AnalyticLocalEvent(
                name = eventName,
                eventData = Gson().toJsonTree(properties),
                eventTimestamp = timeStamp
            )
        return analyticRepository.logEvent(analyticEvent)
    }

    fun flushEvents(): Completable =
        analyticRepository.flushEvents()
}