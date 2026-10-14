package org.stepik.android.domain.course_purchase.model

import org.junit.Assert.*
import org.junit.Test

class CoursePurchaseFlowTest {
    @Test fun everyKnownFlowParsesAndHasTheCorrectInAppFlag() {
        for (flow in CoursePurchaseFlow.entries) {
            assertSame(flow, CoursePurchaseFlow.valueOfWithFallback(flow.name))
            assertEquals(flow != CoursePurchaseFlow.WEB, flow.isInAppActive())
        }
    }

    @Test fun unknownEmptyAndNonCanonicalValuesFallBackToInApp() {
        for (value in listOf("", "unknown", "iap", "web", " IAP ")) {
            assertSame(CoursePurchaseFlow.IAP, CoursePurchaseFlow.valueOfWithFallback(value))
            assertTrue(CoursePurchaseFlow.valueOfWithFallback(value).isInAppActive())
        }
    }
}
