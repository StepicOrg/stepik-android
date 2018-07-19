package org.stepic.droid.util

import org.stepik.android.model.learning.Submission

fun Submission?.getLanguage(): String? = this?.reply?.language