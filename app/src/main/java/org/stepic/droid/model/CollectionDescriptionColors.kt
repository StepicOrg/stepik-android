package org.stepic.droid.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import org.stepic.droid.R
import org.stepik.android.model.CourseCollection

enum class CollectionDescriptionColors(
    @DrawableRes
    val backgroundRes: Int,
    @DrawableRes
    val backgroundResSquared: Int,
    @ColorRes
    val textColorRes: Int
) {
    BLUE(R.drawable.bg_placeholder_blue, R.drawable.gradient_background_blue_squared, R.color.text_color_gradient_blue),
    FIRE(R.drawable.bg_placeholder_fire, R.drawable.gradient_background_fire_squared, R.color.text_color_gradient_fire);

    companion object  {
        fun ofCollection(collection: CourseCollection): CollectionDescriptionColors =
            values()[collection.position % values().size]
    }
}
