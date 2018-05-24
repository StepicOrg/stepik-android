package org.stepic.droid.features.deadlines.model

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import org.stepic.droid.R
import org.stepic.droid.util.AppConstants

enum class LearningRate(
        @StringRes val title: Int,
        @DrawableRes val icon: Int,
        val millisPerWeek: Long
) {
    HOBBY(
            R.string.deadlines_learning_rate_hobby,
            R.drawable.ic_deadlines_learning_rate_hobby,
            AppConstants.MILLIS_IN_1HOUR * 3
    ),
    STANDARD(
            R.string.deadlines_learning_rate_standard,
            R.drawable.ic_deadlines_learning_rate_standard,
            AppConstants.MILLIS_IN_1HOUR * 7
    ),
    EXTREME(
            R.string.deadlines_learning_rate_extreme,
            R.drawable.ic_deadlines_learning_rate_extreme,
            AppConstants.MILLIS_IN_1HOUR * 15
    )
}