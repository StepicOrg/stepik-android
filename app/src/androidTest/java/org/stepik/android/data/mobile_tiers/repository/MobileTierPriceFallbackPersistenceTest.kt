package org.stepik.android.data.mobile_tiers.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.stepik.android.cache.mobile_tiers.LightSkuCacheDataSourceImpl
import org.stepik.android.cache.mobile_tiers.MobileTiersCacheDataSourceImpl
import org.stepik.android.cache.testutil.InMemoryBillingDatabaseTest
import org.stepik.android.cache.testutil.awaitComplete
import org.stepik.android.cache.testutil.awaitValue
import org.stepik.android.data.billing.source.BillingRemoteDataSource
import org.stepik.android.data.mobile_tiers.source.MobileTiersRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.mobile_tiers.model.LightSku
import org.stepik.android.domain.mobile_tiers.model.MobileTier
import org.stepik.android.remote.mobile_tiers.model.MobileTierCalculation

@RunWith(AndroidJUnit4::class)
class MobileTierPriceFallbackPersistenceTest : InMemoryBillingDatabaseTest() {
    @Test fun persistedTiersAndLocalizedPricesSurviveRemoteFailuresAndCacheMissMerge() {
        val tier = MobileTier("test-tier", 42, "test-sku", "test-promo-sku")
        val unrelated = MobileTier("other-tier", 99, "other-sku")
        val prices = listOf(LightSku("test-sku", "12,34 €"), LightSku("test-promo-sku", "١٢٫٣٤ ر.س.\u200f"))
        val tierCache = MobileTiersCacheDataSourceImpl(database.mobileTiersDao())
        val priceCache = LightSkuCacheDataSourceImpl(database.lightSkuDao())
        tierCache.saveMobileTiers(listOf(unrelated)).awaitComplete()
        priceCache.saveLightInventory(prices + LightSku("other-sku", "￥1,200")).awaitComplete()
        val remote = FakeTiersRemote(listOf(tier))
        val calculations = listOf(MobileTierCalculation(42, promo = "ПРОМО"))
        val tiers = MobileTiersRepositoryImpl(remote, tierCache)
        assertEquals(listOf(tier), tiers.calculateMobileTiers(calculations, DataSourceType.REMOTE).awaitValue())
        remote.fail = true
        val offlineTiers = MobileTiersRepositoryImpl(remote, MobileTiersCacheDataSourceImpl(database.mobileTiersDao()))
        val fallback = offlineTiers.calculateMobileTiers(calculations, DataSourceType.REMOTE).awaitValue()
        assertEquals(listOf(tier), fallback)
        val billing = OfflineBillingRemote()
        val offlinePrices = LightSkuRepositoryImpl(LightSkuCacheDataSourceImpl(database.lightSkuDao()), billing)
        val skuIds = listOf(fallback.single().priceTier!!, fallback.single().promoTier!!, "missing-sku")
        assertEquals(prices.toSet(), offlinePrices.getLightInventory(BillingClient.ProductType.INAPP, skuIds, DataSourceType.REMOTE).awaitValue().toSet())
        assertEquals(listOf(BillingClient.ProductType.INAPP to skuIds), billing.requests)
        assertEquals(listOf(unrelated), tierCache.getMobileTiers(listOf(99)).awaitValue())
        assertEquals(listOf(LightSku("other-sku", "￥1,200")), priceCache.getLightInventory(listOf("other-sku")).awaitValue())
        remote.fail = false
        val missingCalculation = MobileTierCalculation(43, promo = "MISSING-PROMO")
        val missingTier = MobileTier("new-tier", 43, "new-sku")
        remote.items = listOf(missingTier)
        assertEquals(setOf(tier, missingTier), offlineTiers.calculateMobileTiers(calculations + missingCalculation, DataSourceType.CACHE).awaitValue().toSet())
        assertEquals(listOf(missingCalculation), remote.requests.last())
        assertEquals(setOf(tier, missingTier), tierCache.getMobileTiers(listOf(42, 43)).awaitValue().toSet())
        val requestsBeforeHit = remote.requests.size
        assertEquals(setOf(tier, missingTier), offlineTiers.calculateMobileTiers(calculations + missingCalculation, DataSourceType.CACHE).awaitValue().toSet())
        assertEquals(requestsBeforeHit, remote.requests.size)
    }

    private class FakeTiersRemote(var items: List<MobileTier>) : MobileTiersRemoteDataSource {
        var fail = false
        val requests = mutableListOf<List<MobileTierCalculation>>()
        override fun getMobileTiers(mobileTierCalculations: List<MobileTierCalculation>) = Single.defer {
            requests += mobileTierCalculations
            if (fail) Single.error(IllegalStateException("synthetic offline")) else Single.just(items)
        }
    }

    private class OfflineBillingRemote : BillingRemoteDataSource {
        val requests = mutableListOf<Pair<String, List<String>>>()
        override fun getInventory(productType: String, skuIds: List<String>): Single<List<ProductDetails>> = Single.defer {
            requests += productType to skuIds
            Single.error(IllegalStateException("synthetic Play unavailable"))
        }
        override fun getAllPurchases(productType: String): Single<List<Purchase>> = Single.error(AssertionError("unexpected purchases"))
        override fun consumePurchase(purchase: Purchase): Completable = Completable.error(AssertionError("unexpected consume"))
    }
}
