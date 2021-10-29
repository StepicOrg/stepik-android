package org.stepik.android.domain.mobile_tiers.repository

import io.reactivex.Single
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.mobile_tiers.model.LightSku

interface LightSkuRepository {
    fun getLightInventory(productType: String, skuIds: List<String>, dataSourceType: DataSourceType = DataSourceType.CACHE): Single<List<LightSku>>
}