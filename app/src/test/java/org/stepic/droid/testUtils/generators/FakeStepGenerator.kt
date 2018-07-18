package org.stepic.droid.testUtils.generators

import org.stepik.android.model.structure.Step

object FakeStepGenerator {
    @JvmOverloads
    fun generate(stepId: Long = 0,
                 status: Step.Status = Step.Status.READY): Step =
            Step(id = stepId, status = status)
}
