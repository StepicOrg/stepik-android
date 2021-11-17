package org.stepik.android.data.mobile_tiers.repository

import io.reactivex.Single
import org.stepik.android.data.billing.source.BillingRemoteDataSource
import org.stepik.android.data.mobile_tiers.source.LightSkuCacheDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.mobile_tiers.model.LightSku
import org.stepik.android.domain.mobile_tiers.repository.LightSkuRepository
import ru.nobird.android.domain.rx.doCompletableOnSuccess
import javax.inject.Inject

class LightSkuRepositoryImpl
@Inject
constructor(
    private val lightSkuCacheDataSource: LightSkuCacheDataSource,
    private val billingRemoteDataSource: BillingRemoteDataSource
) : LightSkuRepository {
    override fun getLightInventory(productType: String, skuIds: List<String>, dataSourceType: DataSourceType): Single<List<LightSku>> {
        val remote = remoteAction(productType, skuIds)

        val cache = lightSkuCacheDataSource
            .getLightInventory(skuIds)

        return when (dataSourceType) {
            DataSourceType.REMOTE ->
                remote
                    .onErrorResumeNext(cache)

            DataSourceType.CACHE ->
                cache.flatMap { cachedItems ->
                    val newIds = (skuIds.toList() - cachedItems.map { it.id })
                    remoteAction(productType, newIds)
                            .map { remoteItems -> (cachedItems + remoteItems) }
                }

            else ->
                throw IllegalArgumentException("Unsupported source type = $dataSourceType")
        }
    }

    private fun remoteAction(productType: String, skuIds: List<String>): Single<List<LightSku>> =
        billingRemoteDataSource
            .getInventory(productType, skuIds)
            .map { inventory -> inventory.map { LightSku(it.id.code, it.price) } }
            .doCompletableOnSuccess(lightSkuCacheDataSource::saveLightInventory)
}