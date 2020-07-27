package org.stepik.android.data.analytic.repository

import io.reactivex.Completable
import org.stepik.android.data.analytic.source.AnalyticCacheDataSource
import org.stepik.android.data.analytic.source.AnalyticRemoteDataSource
import org.stepik.android.domain.analytic.repository.AnalyticRepository
import org.stepik.android.model.analytic.AnalyticLocalEvent
import javax.inject.Inject

class AnalyticRepositoryImpl
@Inject
constructor(
    private val analyticRemoteDataSource: AnalyticRemoteDataSource,
    private val analyticCacheDataSource: AnalyticCacheDataSource
) : AnalyticRepository {
    override fun logEvent(analyticEvent: AnalyticLocalEvent): Completable =
        analyticCacheDataSource.logEvent(analyticEvent)

    override fun flushEvents(): Completable {
        TODO("Not yet implemented")
    }
}