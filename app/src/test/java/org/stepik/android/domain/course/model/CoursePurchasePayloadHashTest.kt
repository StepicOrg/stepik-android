package org.stepik.android.domain.course.model

import org.junit.Assert.*
import org.junit.Test

class CoursePurchasePayloadHashTest {
    @Test fun fixedRestoreIdentifierIncludesProfileThenCourse() {
        val payload = CoursePurchasePayload(71, 42)
        assertEquals("2243", payload.hashCode().toString())
        assertEquals("1754", payload.profileId.toString().hashCode().toString())
        assertEquals(payload.hashCode(), CoursePurchasePayload(71, 42).hashCode())
        assertNotEquals(payload.hashCode(), payload.copy(profileId = 72).hashCode())
        assertNotEquals(payload.hashCode(), payload.copy(courseId = 43).hashCode())
        assertNotEquals(payload.hashCode(), CoursePurchasePayload(42, 71).hashCode())
    }

    @Test fun identifiersRemainDeterministicForLongIdsBeyondIntRange() {
        val payload = CoursePurchasePayload(4_294_967_297, 8_589_934_595)
        // Long.hashCode folds high bits: profile = 0, course = 1, giving persisted profile hash 1.
        assertEquals("1", payload.hashCode().toString())
        assertEquals(payload.hashCode(), payload.copy().hashCode())
    }
}
