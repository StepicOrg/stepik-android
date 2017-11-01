package org.stepic.droid.util

import org.stepic.droid.model.Step
import org.stepic.droid.model.StepStatus

fun Step?.getStepType(): String = this?.block?.name ?: AppConstants.TYPE_NULL

fun Step.isCodeStepReady() =
        this.block?.options != null && this.status == StepStatus.READY