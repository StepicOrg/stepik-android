package org.stepik.android.domain.analytic.repository

import io.reactivex.Completable
import org.stepik.android.model.analytic.AnalyticLocalEvent

interface AnalyticRepository {
    fun logEvent(analyticEvent: AnalyticLocalEvent): Completable
    fun flushEvents(): Completable
}