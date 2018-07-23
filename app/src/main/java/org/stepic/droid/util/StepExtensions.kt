package org.stepic.droid.util

import org.stepik.android.model.Step

fun Step?.getStepType(): String = this?.block?.name ?: AppConstants.TYPE_NULL

fun Step.isCodeStepReady() =
        this.block?.options != null && this.status == Step.Status.READY