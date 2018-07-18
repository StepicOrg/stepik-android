package org.stepic.droid.model

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.everyItem
import org.hamcrest.Matchers.notNullValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.stepik.android.model.structure.Step

@RunWith(RobolectricTestRunner::class)
class StepStatusTest {

    @Test
    fun byNameReady() {
        assertEquals(Step.Status.READY, Step.Status.byName("ready"))
    }

    @Test
    fun byNamePreparing() {
        assertEquals(Step.Status.PREPARING, Step.Status.byName("preparing"))
    }

    @Test
    fun byNameError() {
        assertEquals(Step.Status.ERROR, Step.Status.byName("error"))
    }

    @Test
    fun byNameUnexpectedValue() {
        assertNull(Step.Status.byName("Unexpected-value_-vdalue"))
    }

    @Test
    fun byNameAllValuesResolvedByName() {
        assertThat<List<Step.Status?>>(
                Step.Status.values()
                        .map { Step.Status.byName(it.name) }
                        .toList(),
                everyItem(notNullValue(Step.Status::class.java))
        )
    }

    @Test
    fun saveOrderOfReady() {
        assertEquals("backward compatibility (when write/read from memory)", 0, Step.Status.READY.ordinal)
    }

    @Test
    fun saveOrderOfPreparing() {
        assertEquals("backward compatibility (when write/read from memory)", 1, Step.Status.PREPARING.ordinal)
    }

    @Test
    fun saveOrderOfError() {
        assertEquals("backward compatibility", 2, Step.Status.ERROR.ordinal)
    }

}