package org.stepic.droid.testUtils.generators

import org.stepic.droid.model.Step
import org.stepic.droid.model.StepStatus

object FakeStepGenerator {
    @JvmOverloads
    fun generate(stepId: Long = 0,
                 status: StepStatus = StepStatus.READY): Step {
        val step = Step()
        step.id = stepId
        step.status = status
        return step
    }
}
