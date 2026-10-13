package org.stepik.android.data.course.repository

import org.junit.Assert.*
import org.junit.Test
import org.stepik.android.domain.course_payments.model.DeeplinkPromoCode
import org.stepik.android.presentation.course_purchase.model.CoursePurchaseDataResult

class CoursePurchaseDataRepositoryImplTest {
    @Test fun newRepositoryHasEmptyDefaults() {
        val repository = CoursePurchaseDataRepositoryImpl()
        assertEquals(DeeplinkPromoCode.EMPTY, repository.getDeeplinkPromoCode())
        assertSame(CoursePurchaseDataResult.Empty, repository.getCoursePurchaseData())
    }

    @Test fun saveUpdatesBothValuesAndSubsequentSaveReplacesThem() {
        val repository = CoursePurchaseDataRepositoryImpl()
        val promo = DeeplinkPromoCode("ПРОМО", "12,34", "EUR")
        repository.savePurchaseData(promo, CoursePurchaseDataResult.NotAvailable)
        assertEquals(promo, repository.getDeeplinkPromoCode())
        assertSame(CoursePurchaseDataResult.NotAvailable, repository.getCoursePurchaseData())
        repository.savePurchaseData(DeeplinkPromoCode.EMPTY, CoursePurchaseDataResult.Empty)
        assertEquals(DeeplinkPromoCode.EMPTY, repository.getDeeplinkPromoCode())
        assertSame(CoursePurchaseDataResult.Empty, repository.getCoursePurchaseData())
    }
}
