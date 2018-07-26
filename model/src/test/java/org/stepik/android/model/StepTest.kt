package org.stepik.android.model

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.stepik.android.model.util.assertThatObjectParcelable
import java.util.*

@RunWith(RobolectricTestRunner::class)
class StepTest {
    @Test
    fun stepIsParcelable() {
        val step = Step(id = 0, status = Step.Status.READY)
        step.assertThatObjectParcelable<Step>()
    }

    @Test
    fun stepWithDateParcelable() {
        val step = Step(id = 0, status = Step.Status.READY, updateDate = Date(), createDate = Date())
        step.assertThatObjectParcelable<Step>()
    }
}