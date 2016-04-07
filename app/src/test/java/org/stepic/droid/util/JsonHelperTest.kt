package org.stepic.droid.util

import org.junit.Test

import org.junit.Assert.assertTrue

class JsonHelperTest {

    @Test
    fun toJson_NullObject_ReturnsEmptyString() {
        assertTrue(JsonHelper.toJson(null).isEmpty())
    }
}
