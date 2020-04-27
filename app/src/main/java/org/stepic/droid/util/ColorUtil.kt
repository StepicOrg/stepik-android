package org.stepic.droid.util

import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

import org.stepic.droid.base.App

object ColorUtil {
    @ColorInt
    fun getColorArgb(@ColorRes resourceColor: Int, context: Context = App.getAppContext()): Int =
        ContextCompat.getColor(context, resourceColor)
}
