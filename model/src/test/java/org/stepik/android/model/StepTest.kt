package org.stepik.android.model

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.stepik.android.model.util.assertThatObjectParcelable

@RunWith(RobolectricTestRunner::class)
class StepTest {
    @Test
    fun stepIsParcelable() {
        val step = Step(id = 0, status = Step.Status.READY)
        step.assertThatObjectParcelable<Step>()
    }
}