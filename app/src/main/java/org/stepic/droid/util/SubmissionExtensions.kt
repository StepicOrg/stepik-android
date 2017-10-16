package org.stepic.droid.util

import org.stepic.droid.model.Submission

fun Submission?.getLanguage(): String? = this?.reply?.language