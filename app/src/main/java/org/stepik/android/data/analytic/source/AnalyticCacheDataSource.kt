package org.stepik.android.data.analytic.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.model.analytic.AnalyticLocalEvent

interface AnalyticCacheDataSource {
    fun logEvent(analyticEvent: AnalyticLocalEvent): Completable
    fun getAllEvents(): Single<List<AnalyticLocalEvent>>
    fun clearEvents(): Completable
}