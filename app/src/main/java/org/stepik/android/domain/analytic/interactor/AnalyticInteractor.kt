package org.stepik.android.domain.analytic.interactor

import com.google.gson.Gson
import io.reactivex.Completable
import org.stepik.android.domain.analytic.repository.AnalyticRepository
import org.stepik.android.model.analytic.AnalyticLocalEvent
import timber.log.Timber
import javax.inject.Inject

class AnalyticInteractor
@Inject
constructor(
    private val analyticRepository: AnalyticRepository
) {
    fun logEvent(eventName: String, properties: HashMap<String, String>): Completable {
        val timeStamp = properties.remove("timestamp")?.toLong() ?: 0L
        val analyticEvent = AnalyticLocalEvent(
            name = eventName,
            eventData = Gson().toJsonTree(properties),
            eventTimestamp = timeStamp
        )
        Timber.d("Event name: $eventName Properties: $analyticEvent")
        return analyticRepository.logEvent(analyticEvent)
    }
}