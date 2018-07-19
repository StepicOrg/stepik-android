package org.stepic.droid.util

import org.stepik.android.model.Submission

fun Submission?.getLanguage(): String? = this?.reply?.language