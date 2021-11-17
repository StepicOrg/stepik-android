package org.stepik.android.cache.mobile_tiers

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.cache.mobile_tiers.dao.MobileTiersDao
import org.stepik.android.data.mobile_tiers.source.MobileTiersCacheDataSource
import org.stepik.android.domain.mobile_tiers.model.MobileTier
import javax.inject.Inject

class MobileTiersCacheDataSourceImpl
@Inject
constructor(
    private val mobileTiersDao: MobileTiersDao
) : MobileTiersCacheDataSource {
    override fun getMobileTiers(courseIds: List<Long>): Single<List<MobileTier>> =
        mobileTiersDao.getMobileTiers(courseIds)

    override fun saveMobileTiers(items: List<MobileTier>): Completable =
        mobileTiersDao.saveMobileTiers(items)
}