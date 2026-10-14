package org.stepik.android.domain.course_payments.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.stepik.android.domain.course_payments.model.DefaultPromoCode
import org.stepik.android.model.Course
import java.util.Date

class DefaultPromoCodeMapperTest {
    private val mapper = DefaultPromoCodeMapper()
    private val course = Course(42, defaultPromoCodeName = "ПРОМО", defaultPromoCodePrice = "12,34 €", defaultPromoCodeDiscount = "25%")

    @Test fun allRequiredValuesMapWithOptionalExpiry() {
        for (expiry in listOf(null, Date(1_700_000_000_000))) {
            assertEquals(DefaultPromoCode("ПРОМО", "12,34 €", "25%", expiry), mapper.mapToDefaultPromoCode(course.copy(defaultPromoCodeExpireDate = expiry)))
        }
    }

    @Test fun eachMissingRequiredValueReturnsEmptyEvenWithExpiry() {
        val expiry = Date(1_700_000_000_000)
        for (incomplete in listOf(course.copy(defaultPromoCodeName = null), course.copy(defaultPromoCodePrice = null), course.copy(defaultPromoCodeDiscount = null), Course(42))) {
            assertSame(DefaultPromoCode.EMPTY, mapper.mapToDefaultPromoCode(incomplete.copy(defaultPromoCodeExpireDate = expiry)))
        }
    }

    @Test fun emptyRequiredStringsArePresentValues() {
        assertEquals(DefaultPromoCode("", "", "", null), mapper.mapToDefaultPromoCode(course.copy(
            defaultPromoCodeName = "", defaultPromoCodePrice = "", defaultPromoCodeDiscount = "")))
    }
}
