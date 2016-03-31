package org.stepic.droid.util

import android.content.Context
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat

import org.stepic.droid.base.MainApplication

object   ColorUtil {
    @ColorInt
    fun getColorArgb(@ColorRes resourceColor: Int, context: Context = MainApplication.getAppContext()): Int {
        return ContextCompat.getColor(context, resourceColor)
    }
}
