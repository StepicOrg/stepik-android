package org.stepik.android.view.onboarding.model

import androidx.annotation.DrawableRes

data class OnboardingGoalItem(
    @DrawableRes
    val backgroundResId: Int,
    val itemTitle: String
)
