package org.stepic.droid.core.updating_step.contract

interface UpdatingStepListener {
    fun onNeedUpdate(stepId: Long, isSuccessAttempt: Boolean)
}
