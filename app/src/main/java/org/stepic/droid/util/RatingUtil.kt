package org.stepic.droid.util

import android.os.Bundle
import org.stepic.droid.analytic.Analytic

object RatingUtil {
    fun isExcellent(rating: Int) = rating > 4
}

fun Analytic.reportRateEvent(starNumber: Int, event: String) {
    val bundle = Bundle()
    bundle.putInt("rating", starNumber)
    this.reportEvent(event, bundle)
}