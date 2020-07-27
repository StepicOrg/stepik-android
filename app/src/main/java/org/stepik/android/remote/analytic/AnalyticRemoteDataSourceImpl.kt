package org.stepik.android.remote.analytic

import io.reactivex.Completable
import org.stepik.android.data.analytic.source.AnalyticRemoteDataSource
import org.stepik.android.model.analytic.AnalyticBatchEvent
import org.stepik.android.remote.analytic.model.AnalyticBatchRequest
import org.stepik.android.remote.analytic.service.AnalyticService
import javax.inject.Inject

class AnalyticRemoteDataSourceImpl
@Inject
constructor(
    private val analyticService: AnalyticService
) : AnalyticRemoteDataSource {
    override fun flushEvents(events: List<AnalyticBatchEvent>): Completable =
        analyticService.batch(AnalyticBatchRequest(events))
}