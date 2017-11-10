package org.stepic.droid.model

import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import org.stepic.droid.R

enum class CollectionDescriptionColors(
        @DrawableRes
        val backgroundRes: Int,
        @ColorRes
        val textColorRes: Int
) {
    BLUE(R.drawable.gradient_background_blue, R.color.text_color_gradient_blue),
    FIRE(R.drawable.gradient_background_fire, R.color.text_color_gradient_fire)
}
