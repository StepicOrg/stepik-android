package org.stepik.android.data.analytic.source

import io.reactivex.Completable
import org.stepik.android.remote.analytic.model.AnalyticBatchEvent

interface AnalyticRemoteDataSource {
    fun flushEvents(events: List<AnalyticBatchEvent>): Completable
}