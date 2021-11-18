package org.stepik.android.cache.mobile_tiers

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.cache.mobile_tiers.dao.LightSkuDao
import org.stepik.android.data.mobile_tiers.source.LightSkuCacheDataSource
import org.stepik.android.domain.mobile_tiers.model.LightSku
import javax.inject.Inject

class LightSkuCacheDataSourceImpl
@Inject
constructor(
    private val lightSkuDao: LightSkuDao
) : LightSkuCacheDataSource {
    override fun getLightInventory(skuIds: List<String>): Single<List<LightSku>> =
        lightSkuDao.getLightSkus(skuIds)

    override fun saveLightInventory(lightSkus: List<LightSku>): Completable =
        lightSkuDao.saveLightSkus(lightSkus)
}