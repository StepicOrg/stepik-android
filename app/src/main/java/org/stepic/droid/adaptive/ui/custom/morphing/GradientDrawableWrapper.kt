package org.stepic.droid.adaptive.ui.custom.morphing

import android.graphics.drawable.GradientDrawable

class GradientDrawableWrapper(val drawable: GradientDrawable) {

    var color = 0x0
        set(value) {
            field = value
            drawable.setColor(value)
        }

    var cornerRadius = 0f
        set(value) {
            field = value
            drawable.cornerRadius = value
        }
}