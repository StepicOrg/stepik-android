package org.stepic.droid.util

import org.stepic.droid.model.Step

fun Step?.getStepType(): String = this?.block?.name ?: AppConstants.TYPE_NULL