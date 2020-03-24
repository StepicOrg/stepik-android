package org.stepik.android.view.base.ui.extension

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import org.stepic.droid.util.resolveResourceIdAttribute

fun Drawable.setTintList(context: Context, @AttrRes attr: Int): Drawable {
    val colorStateList = ContextCompat.getColorStateList(context, context.resolveResourceIdAttribute(attr))
    val drawable = this.mutate()
    DrawableCompat.setTintList(drawable, colorStateList)
    return drawable
}