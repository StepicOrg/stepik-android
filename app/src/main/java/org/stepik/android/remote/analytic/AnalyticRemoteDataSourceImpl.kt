package org.stepik.android.remote.analytic

import io.reactivex.Completable
import org.stepik.android.data.analytic.source.AnalyticRemoteDataSource
import org.stepik.android.remote.analytic.model.AnalyticBatchEvent
import org.stepik.android.remote.analytic.service.AnalyticService
import javax.inject.Inject

class AnalyticRemoteDataSourceImpl
@Inject
constructor(
    private val analyticService: AnalyticService
) : AnalyticRemoteDataSource {
    override fun flushEvents(events: List<AnalyticBatchEvent>): Completable =
        if (events.isEmpty()) {
            Completable.complete()
        } else {
            analyticService.batch(events)
        }
}