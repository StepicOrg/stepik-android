package org.stepic.droid.adaptive.model

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import org.stepic.droid.R
import org.stepic.droid.adaptive.ui.fragments.AdaptiveProgressFragment
import org.stepic.droid.adaptive.ui.fragments.AdaptiveRatingFragment

enum class AdaptiveStatsTabs(
    val fragmentFactory: (courseId: Long) -> Fragment,
    @StringRes
    val fragmentTitleRes: Int
) {
    PROGRESS(AdaptiveProgressFragment.Companion::newInstance, R.string.adaptive_progress),
    RATING(AdaptiveRatingFragment.Companion::newInstance, R.string.adaptive_rating)
}