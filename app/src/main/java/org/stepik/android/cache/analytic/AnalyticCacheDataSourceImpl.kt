package org.stepik.android.cache.analytic

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.storage.dao.IDao
import org.stepik.android.cache.analytic.model.AnalyticLocalEvent
import org.stepik.android.data.analytic.source.AnalyticCacheDataSource
import timber.log.Timber
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

    override fun getAllEvents(): Single<List<AnalyticLocalEvent>> =
        Single.fromCallable {
            analyticDao.getAll()
        }

    override fun clearEvents(): Completable =
        Completable.fromCallable {
            analyticDao.removeAll()
        }
}