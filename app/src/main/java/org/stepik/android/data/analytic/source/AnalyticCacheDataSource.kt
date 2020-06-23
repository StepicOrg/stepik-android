package org.stepik.android.data.analytic.source

import io.reactivex.Completable
import org.stepik.android.model.analytic.AnalyticLocalEvent

interface AnalyticCacheDataSource {
    fun logEvent(analyticEvent: AnalyticLocalEvent): Completable
    fun clearEvents(): Completable
}