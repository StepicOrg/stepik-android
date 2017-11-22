package org.stepic.droid.core.updatingstep.contract

interface UpdatingStepListener {
    fun onNeedUpdate(stepId: Long, isSuccessAttempt: Boolean)
}
