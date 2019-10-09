package org.stepic.droid.util

import android.content.Context
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import androidx.core.content.ContextCompat

import org.stepic.droid.base.App

object   ColorUtil {
    @ColorInt
    fun getColorArgb(@ColorRes resourceColor: Int, context: Context = App.getAppContext()): Int {
        return ContextCompat.getColor(context, resourceColor)
    }
}
