package org.stepik.android.data.course_purchase.repository

import com.nhaarman.mockitokotlin2.*
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Assert.*
import org.junit.Test
import org.stepik.android.data.course_purchase.source.BillingPurchasePayloadCacheDataSource
import org.stepik.android.domain.course_purchase.model.BillingPurchasePayload

class BillingPurchasePayloadRepositoryImplTest {
    private val cache = mock<BillingPurchasePayloadCacheDataSource>()
    private val repository = BillingPurchasePayloadRepositoryImpl(cache)
    private val payload = BillingPurchasePayload("test-order", 42, 71, "test-account", "test-profile", "ПРОМО")

    @Test fun forwardsArgumentsAndDefersGetSaveDeleteSideEffectsUntilSubscription() {
        val events = mutableListOf<String>()
        whenever(cache.getPurchaseOrderPayload(payload.orderId)).thenReturn(Single.defer { events += "get"; Single.just(payload) })
        whenever(cache.savePurchaseOrderPayload(payload)).thenReturn(Completable.defer { events += "save"; Completable.complete() })
        whenever(cache.deletePurchaseOrderPayload(payload.orderId)).thenReturn(Completable.defer { events += "delete"; Completable.complete() })
        val get = repository.getBillingPurchasePayload(payload.orderId)
        val save = repository.saveBillingPurchasePayload(payload)
        val delete = repository.deleteBillingPurchasePayload(payload.orderId)
        assertTrue(events.isEmpty())
        save.test().assertResult()
        get.test().assertResult(payload)
        delete.test().assertResult()
        assertEquals(listOf("save", "get", "delete"), events)
        verify(cache).savePurchaseOrderPayload(payload)
        verify(cache).getPurchaseOrderPayload(payload.orderId)
        verify(cache).deletePurchaseOrderPayload(payload.orderId)
        verifyNoMoreInteractions(cache)
    }

    @Test fun originalErrorsPropagateFromAllThreeOperations() {
        val failure = IllegalStateException("synthetic cache failure")
        whenever(cache.getPurchaseOrderPayload(payload.orderId)).thenReturn(Single.error(failure))
        whenever(cache.savePurchaseOrderPayload(payload)).thenReturn(Completable.error(failure))
        whenever(cache.deletePurchaseOrderPayload(payload.orderId)).thenReturn(Completable.error(failure))
        repository.getBillingPurchasePayload(payload.orderId).test().assertNoValues().assertError { it === failure }
        repository.saveBillingPurchasePayload(payload).test().assertNotComplete().assertError { it === failure }
        repository.deleteBillingPurchasePayload(payload.orderId).test().assertNotComplete().assertError { it === failure }
    }
}
