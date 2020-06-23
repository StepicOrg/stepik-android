package org.stepik.android.cache.analytic

import io.reactivex.Completable
import org.stepic.droid.storage.dao.IDao
import org.stepik.android.data.analytic.source.AnalyticCacheDataSource
import org.stepik.android.model.analytic.AnalyticLocalEvent
import javax.inject.Inject

class AnalyticCacheDataSourceImpl
@Inject
constructor(
    private val analyticDao: IDao<AnalyticLocalEvent>
) : AnalyticCacheDataSource {
    override fun logEvent(analyticEvent: AnalyticLocalEvent): Completable =
        Completable.fromCallable {
            analyticDao.insertOrReplace(analyticEvent)
        }

    override fun clearEvents(): Completable =
        Completable.fromCallable {
            analyticDao.removeAll()
        }
}