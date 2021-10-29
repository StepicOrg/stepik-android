package org.stepik.android.data.mobile_tiers.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.mobile_tiers.model.LightSku

interface LightSkuCacheDataSource {
    fun getLightInventory(skuIds: List<String>): Single<List<LightSku>>
    fun saveLightInventory(lightSkus: List<LightSku>): Completable
}