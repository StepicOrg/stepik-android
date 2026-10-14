package org.stepik.android.cache.mobile_tiers

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.stepik.android.cache.testutil.InMemoryBillingDatabaseTest
import org.stepik.android.cache.testutil.awaitComplete
import org.stepik.android.cache.testutil.awaitValue
import org.stepik.android.domain.mobile_tiers.model.LightSku

@RunWith(AndroidJUnit4::class)
class LightSkuDaoTest : InMemoryBillingDatabaseTest() {
    @Test
    fun roundTripsFormattedUnicodeLocalizedPricesExactly() {
        val items = listOf(
            LightSku("test-eur", "1\u202f234,56\u00a0€"), LightSku("test-rub", "999,00\u00a0₽"),
            LightSku("test-jpy", "￥1,200"), LightSku("test-arabic", "١٢٫٣٤ ر.س.\u200f"), LightSku("test-empty", "")
        )
        val dao = database.lightSkuDao()
        dao.saveLightSkus(items).awaitComplete()
        assertEquals(items.toSet(), dao.getLightSkus(items.map { it.id }).awaitValue().toSet())
    }

    @Test
    fun replacementBySkuIdChangesPriceAndPreservesOtherSkus() {
        val dao = database.lightSkuDao()
        val first = LightSku("test-sku-1", "$1.00")
        val other = LightSku("test-sku-2", "$2.00")
        val replacement = first.copy(price = "3,00 €")
        dao.saveLightSkus(listOf(first, other)).awaitComplete()
        dao.saveLightSkus(listOf(replacement)).awaitComplete()
        assertEquals(setOf(replacement, other), dao.getLightSkus(listOf(first.id, other.id)).awaitValue().toSet())
    }

    @Test
    fun partialMissingEmptyAndDuplicateLookupsDoNotFabricateOrDuplicateRows() {
        val dao = database.lightSkuDao()
        val first = LightSku("test-sku-1", "$1.00")
        val other = LightSku("test-sku-2", "$2.00")
        dao.saveLightSkus(listOf(first, other, first)).awaitComplete()
        assertEquals(listOf(first), dao.getLightSkus(listOf(first.id, first.id, "test-missing")).awaitValue())
        assertEquals(emptyList<LightSku>(), dao.getLightSkus(listOf("test-missing")).awaitValue())
        assertEquals(emptyList<LightSku>(), dao.getLightSkus(emptyList()).awaitValue())
        assertEquals(2, dao.getLightSkus(listOf(first.id, other.id)).awaitValue().size)
    }

    @Test
    fun emptyDaoAndCacheSavesPreserveExistingPrices() {
        val dao = database.lightSkuDao()
        val cache = LightSkuCacheDataSourceImpl(dao)
        val item = LightSku("test-sku", "5,99 €")
        cache.saveLightInventory(listOf(item)).awaitComplete()
        dao.saveLightSkus(emptyList()).awaitComplete()
        cache.saveLightInventory(emptyList()).awaitComplete()
        assertEquals(listOf(item), cache.getLightInventory(listOf(item.id, "test-missing")).awaitValue())
    }
}
