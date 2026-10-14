package org.stepik.android.domain.mobile_tiers.interactor

import com.android.billingclient.api.BillingClient
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Single
import org.junit.Test
import org.mockito.Mockito.verifyNoInteractions
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.mobile_tiers.model.LightSku
import org.stepik.android.domain.mobile_tiers.model.MobileTier
import org.stepik.android.domain.mobile_tiers.repository.LightSkuRepository
import org.stepik.android.domain.mobile_tiers.repository.MobileTiersRepository
import org.stepik.android.model.Course
import org.stepik.android.remote.mobile_tiers.model.MobileTierCalculation

class MobileTiersInteractorTest {
    private val tiers = mock<MobileTiersRepository>()
    private val prices = mock<LightSkuRepository>()
    private val interactor = MobileTiersInteractor(tiers, prices)

    @Test fun onlyPaidCoursesBecomeCalculationsAndStandardPromoIdsAreDeduplicated() {
        val courses = listOf(Course(99), Course(42, isPaid = true), Course(43, isPaid = true))
        val calculations = listOf(MobileTierCalculation(42), MobileTierCalculation(43))
        val result = listOf(MobileTier("a", 42, "shared", "promo"), MobileTier("b", 43, "shared", "shared"), MobileTier("c", 44))
        val inventory = listOf(LightSku("shared", "12,34 €"), LightSku("promo", "9,99 €"))
        whenever(tiers.calculateMobileTiers(calculations, DataSourceType.REMOTE)).thenReturn(Single.just(result))
        whenever(prices.getLightInventory(BillingClient.ProductType.INAPP, listOf("shared", "promo"), DataSourceType.CACHE)).thenReturn(Single.just(inventory))
        interactor.fetchTiersAndSkus(courses, DataSourceType.CACHE).test().assertResult(result to inventory)
        verify(tiers).calculateMobileTiers(calculations, DataSourceType.REMOTE)
        verify(prices).getLightInventory(BillingClient.ProductType.INAPP, listOf("shared", "promo"), DataSourceType.CACHE)
    }

    @Test fun knownSourceBehaviorAlwaysCalculatesTiersRemotelyWhilePricesReceiveRequestedSource() {
        for (source in DataSourceType.entries) {
            val calculations = listOf(MobileTierCalculation(42))
            whenever(tiers.calculateMobileTiers(calculations, DataSourceType.REMOTE)).thenReturn(Single.just(emptyList()))
            whenever(prices.getLightInventory(BillingClient.ProductType.INAPP, emptyList(), source)).thenReturn(Single.just(emptyList()))
            interactor.fetchTiersAndSkus(listOf(Course(42, isPaid = true)), source).test().assertResult(emptyList<MobileTier>() to emptyList<LightSku>())
            verify(prices).getLightInventory(BillingClient.ProductType.INAPP, emptyList(), source)
        }
        verify(tiers, times(2)).calculateMobileTiers(listOf(MobileTierCalculation(42)), DataSourceType.REMOTE)
    }

    @Test fun noPaidCoursesIncludingEmptyInputProducesValidEmptyPair() {
        whenever(tiers.calculateMobileTiers(emptyList(), DataSourceType.REMOTE)).thenReturn(Single.just(emptyList()))
        whenever(prices.getLightInventory(BillingClient.ProductType.INAPP, emptyList(), DataSourceType.REMOTE)).thenReturn(Single.just(emptyList()))
        for (courses in listOf(emptyList(), listOf(Course(42), Course(43)))) {
            interactor.fetchTiersAndSkus(courses, DataSourceType.REMOTE).test().assertResult(emptyList<MobileTier>() to emptyList<LightSku>())
        }
    }

    @Test fun nullTiersAndPartialInventoryDoNotFabricatePrices() {
        val calculations = listOf(MobileTierCalculation(42))
        for (result in listOf(emptyList(), listOf(MobileTier("null", 42)), listOf(MobileTier("tier", 42, "standard", "promo")))) {
            val ids = if (result.singleOrNull()?.priceTier == "standard") listOf("standard", "promo") else emptyList()
            val inventory = if (ids.isEmpty()) emptyList() else listOf(LightSku("standard", "12,34 €"))
            whenever(tiers.calculateMobileTiers(calculations, DataSourceType.REMOTE)).thenReturn(Single.just(result))
            whenever(prices.getLightInventory(BillingClient.ProductType.INAPP, ids, DataSourceType.REMOTE)).thenReturn(Single.just(inventory))
            interactor.fetchTiersAndSkus(listOf(Course(42, isPaid = true)), DataSourceType.REMOTE).test().assertResult(result to inventory)
        }
    }

    @Test fun tierFailureReturnsEmptyPairWithoutPriceLookup() {
        whenever(tiers.calculateMobileTiers(listOf(MobileTierCalculation(42)), DataSourceType.REMOTE)).thenReturn(Single.error(IllegalStateException("synthetic tier failure")))
        interactor.fetchTiersAndSkus(listOf(Course(42, isPaid = true)), DataSourceType.CACHE).test().assertResult(emptyList<MobileTier>() to emptyList<LightSku>())
        verifyNoInteractions(prices)
    }

    @Test fun priceFailureDropsBothListsAndReturnsEmptyPair() {
        whenever(tiers.calculateMobileTiers(listOf(MobileTierCalculation(42)), DataSourceType.REMOTE))
            .thenReturn(Single.just(listOf(MobileTier("tier", 42, "standard"))))
        whenever(prices.getLightInventory(BillingClient.ProductType.INAPP, listOf("standard"), DataSourceType.CACHE))
            .thenReturn(Single.error(IllegalStateException("synthetic price failure")))
        interactor.fetchTiersAndSkus(listOf(Course(42, isPaid = true)), DataSourceType.CACHE).test().assertResult(emptyList<MobileTier>() to emptyList<LightSku>())
    }
}
