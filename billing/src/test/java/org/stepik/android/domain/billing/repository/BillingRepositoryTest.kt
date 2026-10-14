package org.stepik.android.domain.billing.repository

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Test
import org.stepik.android.testutil.BillingFixtures

class BillingRepositoryTest {
    private class Repository : BillingRepository {
        var inventory: Single<List<ProductDetails>> = Single.just(emptyList())
        var purchases: Single<List<Purchase>> = Single.just(emptyList())
        var inventoryRequest: Pair<String, List<String>>? = null
        var purchaseType: String? = null
        override fun getInventory(productType: String, skuIds: List<String>): Single<List<ProductDetails>> {
            inventoryRequest = productType to skuIds
            return inventory
        }
        override fun getAllPurchases(productType: String): Single<List<Purchase>> {
            purchaseType = productType
            return purchases
        }
        override fun consumePurchase(purchase: Purchase): Completable = error("Not needed for default contracts")
    }

    private val repository = Repository()
    private val type = BillingClient.ProductType.INAPP

    @Test fun singleInventoryForwardsSingletonRequestAndSelectsFirstItem() {
        val first = BillingFixtures.product("course.first")
        repository.inventory = Single.just(listOf(first, BillingFixtures.product("course.second")))
        repository.getInventory(type, "course.first").test().assertResult(first)
        assertEquals(type to listOf("course.first"), repository.inventoryRequest)
    }

    @Test fun absentInventoryCompletesEmptyMaybe() {
        repository.getInventory(type, "course.absent").test().assertResult()
    }

    @Test fun inventoryErrorPropagates() {
        val failure = IllegalStateException("synthetic inventory error")
        repository.inventory = Single.error(failure)
        repository.getInventory(type, "course.absent").test().assertError { it === failure }.assertNoValues()
    }

    @Test fun filtersPurchasesByAnyProductSafelyAndWithoutDuplicates() {
        val first = BillingFixtures.purchase(listOf("course.a"))
        val later = BillingFixtures.purchase(listOf("course.other", "course.b"))
        val both = BillingFixtures.purchase(listOf("course.a", "course.b"))
        val empty = BillingFixtures.purchase(emptyList())
        val unrelated = BillingFixtures.purchase(listOf("course.other"))
        repository.purchases = Single.just(listOf(first, later, both, empty, unrelated))
        repository.getAllPurchases(type, listOf("course.a", "course.b", "course.a")).test().assertResult(listOf(first, later, both))
        assertEquals(type, repository.purchaseType)
    }

    @Test fun emptyRequestedProductsAndEmptyPurchasesReturnEmpty() {
        repository.purchases = Single.just(listOf(BillingFixtures.purchase(), BillingFixtures.purchase(emptyList())))
        repository.getAllPurchases(type, emptyList()).test().assertResult(emptyList())
        repository.purchases = Single.just(emptyList())
        repository.getAllPurchases(type, listOf("course.a")).test().assertResult(emptyList())
    }

    @Test fun purchaseErrorPropagates() {
        val failure = IllegalStateException("synthetic purchase error")
        repository.purchases = Single.error(failure)
        repository.getAllPurchases(type, listOf("course.a")).test().assertError { it === failure }.assertNoValues()
    }
}
