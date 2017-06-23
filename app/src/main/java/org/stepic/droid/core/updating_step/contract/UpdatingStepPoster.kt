package org.stepic.droid.core.updating_step.contract

interface UpdatingStepPoster {
    fun updateStep(stepId: Long, isSuccessAttempt: Boolean)
}
