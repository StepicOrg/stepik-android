package org.stepik.android.data.analytic.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.cache.analytic.model.AnalyticLocalEvent

interface AnalyticCacheDataSource {
    fun logEvent(analyticEvent: AnalyticLocalEvent): Completable
    fun getEvents(): Single<List<AnalyticLocalEvent>>
    fun clearEvents(analyticEvents: List<AnalyticLocalEvent>): Completable
}