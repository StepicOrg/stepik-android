package org.stepic.droid.util

import org.stepik.android.model.VideoUrl

fun VideoUrl.greaterThanMaxQuality(): Boolean =
    try {
        Integer.parseInt(this.quality!!) > AppConstants.MAX_QUALITY_INT
    } catch (exception: Exception) {
        true
    }

