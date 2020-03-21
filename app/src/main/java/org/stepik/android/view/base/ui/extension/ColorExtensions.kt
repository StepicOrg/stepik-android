package org.stepik.android.view.base.ui.extension

import androidx.annotation.ColorInt

object ColorExtensions {
    private const val MASK_OPACITY = 0xFF
    private const val MASK_NO_OPACITY = 0x00FFFFFF

    fun colorWithAlpha(@ColorInt color: Int, alpha: Float): Int =
        (alpha * MASK_OPACITY).toInt().shl(24) or (color and MASK_NO_OPACITY)
}