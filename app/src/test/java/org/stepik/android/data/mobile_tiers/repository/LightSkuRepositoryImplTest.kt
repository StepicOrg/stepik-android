package org.stepik.android.data.mobile_tiers.repository

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.CompletableSubject
import org.junit.Assert.*
import org.junit.Test
import org.stepik.android.data.billing.source.BillingRemoteDataSource
import org.stepik.android.data.mobile_tiers.source.LightSkuCacheDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.mobile_tiers.model.LightSku
import org.stepik.android.testutil.BillingFixtures

class LightSkuRepositoryImplTest {
    private val remote = mock<BillingRemoteDataSource>()
    private val cache = mock<LightSkuCacheDataSource>()
    private val repository = LightSkuRepositoryImpl(cache, remote)
    private val type = BillingClient.ProductType.INAPP
    private val ids = listOf("sku-1", "sku-2")
    private val first = LightSku("sku-1", "cached price")
    private val second = LightSku("sku-2", "12,34 €")
    private val failure = IllegalStateException("synthetic failure")
    private val events = mutableListOf<String>()

    private fun setup(cached: List<LightSku> = listOf(first), products: List<ProductDetails> = listOf(BillingFixtures.product("sku-2")), remoteError: Throwable? = null, readError: Throwable? = null, saveError: Throwable? = null) {
        whenever(cache.getLightInventory(ids)).thenReturn(Single.defer {
            events += "read"
            if (readError == null) Single.just(cached) else Single.error(readError)
        })
        whenever(remote.getInventory(eq(type), any())).thenAnswer { invocation ->
            val requested = invocation.getArgument<List<String>>(1)
            Single.defer {
                events += "inventory:$requested"
                if (remoteError != null) Single.error<List<ProductDetails>>(remoteError)
                else Single.just(if (requested.isEmpty()) emptyList() else products)
            }
        }
        whenever(cache.saveLightInventory(any())).thenAnswer {
            Completable.defer { events += "save"; if (saveError == null) Completable.complete() else Completable.error(saveError) }
        }
    }

    @Test fun remoteMapsIdAndExactFormattedPriceAndWaitsForCacheWrite() {
        val product = BillingFixtures.product("sku-1")
        whenever(product.oneTimePurchaseOfferDetails!!.formattedPrice).thenReturn("١٢٫٣٤ ر.س.\u200f")
        val mapped = LightSku("sku-1", "١٢٫٣٤ ر.س.\u200f")
        setup(products = listOf(product))
        val save = CompletableSubject.create()
        whenever(cache.saveLightInventory(listOf(mapped))).thenReturn(save)
        val observer = repository.getLightInventory(type, ids, DataSourceType.REMOTE).test()
        try {
            observer.assertNoValues().assertNotComplete()
            assertTrue(save.hasObservers())
            assertEquals(listOf("inventory:$ids"), events)
            save.onComplete()
            observer.assertResult(listOf(mapped))
            verify(remote).getInventory(type, ids)
        } finally { observer.dispose() }
    }

    @Test fun remoteInventoryMappingAndSaveFailuresAllFallBackToCache() {
        for (stage in listOf("inventory", "mapping", "save")) {
            setup(products = listOf(BillingFixtures.product("sku-2", hasOffer = stage != "mapping")),
                remoteError = if (stage == "inventory") failure else null,
                saveError = if (stage == "save") failure else null)
            events.clear()
            repository.getLightInventory(type, ids, DataSourceType.REMOTE).test().assertResult(listOf(first))
            assertEquals(listOf("inventory:$ids") + (if (stage == "save") listOf("save") else emptyList()) + "read", events)
        }
    }

    @Test fun fallbackCacheFailureIsPreserved() {
        val readFailure = IllegalArgumentException("cache unavailable")
        setup(remoteError = failure, readError = readFailure)
        repository.getLightInventory(type, ids, DataSourceType.REMOTE).test().assertNoValues().assertError { it === readFailure }
    }

    @Test fun cacheFetchesOnlyMissingIdsAndMergesCacheFirstWithoutDuplicatingHits() {
        setup()
        repository.getLightInventory(type, ids, DataSourceType.CACHE).test().assertResult(listOf(first, second))
        verify(remote).getInventory(type, listOf("sku-2"))
        verify(cache).saveLightInventory(listOf(second))
        assertEquals(listOf("read", "inventory:[sku-2]", "save"), events)
    }

    @Test fun fullCacheHitSafelyHandlesEmptyInventoryResult() {
        setup(cached = listOf(second, first))
        repository.getLightInventory(type, ids, DataSourceType.CACHE).test().assertResult(listOf(second, first))
        verify(remote).getInventory(type, emptyList())
        verify(cache).saveLightInventory(emptyList())
        assertEquals(listOf("read", "inventory:[]", "save"), events)
    }

    @Test fun partialAndEmptyRemoteInventoryRemainPartialInBothModes() {
        for (mode in DataSourceType.entries) for (products in listOf(emptyList(), listOf(BillingFixtures.product("sku-2")))) {
            setup(products = products)
            val mapped = if (products.isEmpty()) emptyList() else listOf(second)
            repository.getLightInventory(type, ids, mode).test().assertResult(if (mode == DataSourceType.CACHE) listOf(first) + mapped else mapped)
        }
    }

    @Test fun cacheReadFailureSkipsInventorySubscriptionAndWrites() {
        setup(readError = failure)
        repository.getLightInventory(type, ids, DataSourceType.CACHE).test().assertNoValues().assertError { it === failure }
        assertEquals(listOf("read"), events)
        verify(cache, never()).saveLightInventory(any())
    }

    @Test fun cacheInventoryAndWriteFailuresPropagateWithoutFallback() {
        for (writeFailure in listOf(false, true)) {
            setup(remoteError = if (writeFailure) null else failure, saveError = if (writeFailure) failure else null)
            events.clear()
            repository.getLightInventory(type, ids, DataSourceType.CACHE).test().assertNoValues().assertError { it === failure }
            assertEquals(listOf("read", "inventory:[sku-2]") + if (writeFailure) listOf("save") else emptyList(), events)
        }
    }

    @Test fun missingOfferInCacheMissErrorsWithoutSavingInvalidPricesOrRereadingCache() {
        setup(products = listOf(BillingFixtures.product("sku-2", hasOffer = false)))
        repository.getLightInventory(type, ids, DataSourceType.CACHE).test().assertNoValues().assertError(IllegalArgumentException::class.java)
        assertEquals(listOf("read", "inventory:[sku-2]"), events)
        verify(cache, never()).saveLightInventory(any())
    }
}
