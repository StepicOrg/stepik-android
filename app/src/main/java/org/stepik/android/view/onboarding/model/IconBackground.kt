package org.stepik.android.view.onboarding.model

import androidx.annotation.DrawableRes
import org.stepic.droid.R

enum class IconBackground(
    @DrawableRes
    val backgroundRes: Int
) {
    YELLOW_RED(R.drawable.onboarding_goal_yellow_red_gradient),
    BLUE_VIOLET(R.drawable.onboarding_goal_blue_violet_gradient),
    YELLOW_GREEN(R.drawable.onboarding_goal_yellow_green_gradient)
}