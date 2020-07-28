package org.stepik.android.domain.analytic.repository

import io.reactivex.Completable
import org.stepik.android.cache.analytic.model.AnalyticLocalEvent

interface AnalyticRepository {
    fun logEvent(analyticEvent: AnalyticLocalEvent): Completable
    fun flushEvents(): Completable
}