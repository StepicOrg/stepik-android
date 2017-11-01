package org.stepic.droid.storage.operations

import org.stepic.droid.model.StepInfo

interface StepInfoOperation {

    fun getStepInfo(stepIds: List<Long>): List<StepInfo>
}
