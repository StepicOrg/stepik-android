package org.stepic.droid.model

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.everyItem
import org.hamcrest.Matchers.notNullValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.stepic.droid.testUtils.assertThatObjectParcelable

@RunWith(RobolectricTestRunner::class)
class StepStatusTest {

    @Test
    fun preparingParcelable() {
        StepStatus.PREPARING.assertThatObjectParcelable<StepStatus>()
    }

    @Test
    fun readyParcelable() {
        StepStatus.READY.assertThatObjectParcelable<StepStatus>()
    }

    @Test
    fun byNameReady() {
        assertEquals(StepStatus.READY, StepStatus.Helper.byName("ready"))
    }

    @Test
    fun byNamePreparing() {
        assertEquals(StepStatus.PREPARING, StepStatus.Helper.byName("preparing"))
    }

    @Test
    fun byNameError() {
        assertEquals(StepStatus.ERROR, StepStatus.Helper.byName("error"))
    }

    @Test
    fun byNameUnexpectedValue() {
        assertNull(StepStatus.Helper.byName("Unexpected-value_-vdalue"))
    }

    @Test
    fun byNameAllValuesResolvedByName() {
        assertThat<List<StepStatus?>>(
                StepStatus.values()
                        .map { StepStatus.Helper.byName(it.name) }
                        .toList(),
                everyItem(notNullValue(StepStatus::class.java))
        )
    }

    @Test
    fun saveOrderOfReady() {
        assertEquals("backward compatibility (when write/read from memory)", 0, StepStatus.READY.ordinal)
    }

    @Test
    fun saveOrderOfPreparing() {
        assertEquals("backward compatibility (when write/read from memory)", 1, StepStatus.PREPARING.ordinal)
    }

    @Test
    fun saveOrderOfError() {
        assertEquals("backward compatibility", 1, StepStatus.ERROR.ordinal)
    }

}