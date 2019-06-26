package org.stepic.droid.util

import android.graphics.drawable.GradientDrawable

fun GradientDrawable.setTopRoundedCorners(radius: Float) {
    val radiiArray = FloatArray(8)
    (0 until 4).forEach { radiiArray[it] = radius }
    this.cornerRadii = radiiArray
}

fun GradientDrawable.setRoundedCorners(radius: Float) {
    this.cornerRadii = FloatArray(8) { radius }
}