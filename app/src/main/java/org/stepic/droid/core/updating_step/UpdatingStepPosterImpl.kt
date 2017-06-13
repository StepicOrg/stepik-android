package org.stepic.droid.core.updating_step

import org.stepic.droid.base.ListenerContainer
import org.stepic.droid.core.updating_step.contract.UpdatingStepListener
import org.stepic.droid.core.updating_step.contract.UpdatingStepPoster
import javax.inject.Inject

class UpdatingStepPosterImpl
@Inject constructor(
        private val listenerContainer: ListenerContainer<UpdatingStepListener>)
    : UpdatingStepPoster {

    override fun updateStep(stepId: Long, isSuccessAttempt: Boolean) {
        listenerContainer.asIterable().forEach { it.onNeedUpdate(stepId, isSuccessAttempt) }
    }
}
