package org.stepic.droid.core.updatingstep.contract

interface UpdatingStepPoster {
    fun updateStep(stepId: Long, isSuccessAttempt: Boolean)
}
