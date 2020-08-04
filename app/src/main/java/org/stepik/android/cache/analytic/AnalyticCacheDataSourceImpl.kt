package org.stepik.android.cache.analytic

import io.reactivex.Completable
import io.reactivex.Observable
import org.stepik.android.cache.analytic.dao.AnalyticDao
import org.stepik.android.cache.analytic.model.AnalyticLocalEvent
import org.stepik.android.data.analytic.source.AnalyticCacheDataSource
import javax.inject.Inject

class AnalyticCacheDataSourceImpl
@Inject
constructor(
    private val analyticDao: AnalyticDao
) : AnalyticCacheDataSource {
    override fun logEvent(analyticEvent: AnalyticLocalEvent): Completable =
        analyticDao.insertAnalyticEvent(analyticEvent)

    override fun getEvents(): Observable<List<AnalyticLocalEvent>> =
        analyticDao.getAnalyticEvents()

    override fun clearEvents(analyticEvents: List<AnalyticLocalEvent>): Completable =
        analyticDao.clearEvents(analyticEvents)
}