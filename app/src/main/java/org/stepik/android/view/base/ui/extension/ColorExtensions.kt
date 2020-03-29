package org.stepik.android.view.base.ui.extension

import androidx.annotation.ColorInt
import androidx.annotation.Dimension

object ColorExtensions {
    private const val MASK_OPACITY = 0xFF
    private const val MASK_NO_OPACITY = 0x00FFFFFF

    fun colorWithAlpha(@ColorInt color: Int, alpha: Float): Int =
        (alpha * MASK_OPACITY).toInt().shl(24) or (color and MASK_NO_OPACITY)

    /**
     * Multiplies current alpha channel with given [alpha]
     */
    fun colorWithAlphaMul(@ColorInt color: Int, alpha: Float): Int =
        (alpha * color.shr(24)).toInt().shl(24) or (color and MASK_NO_OPACITY)

    /**
     * Mixes [color1] with [color2] in additive way
     */
    fun add(@ColorInt color1: Int, @ColorInt color2: Int): Int =
        ((color1 shr 24) shl 24) or addChannel(0, color1, color2) or addChannel(8, color1, color2) or addChannel(16, color1, color2)

    private fun addChannel(shift: Int, @ColorInt color1: Int, @ColorInt color2: Int): Int =
        ((color1.shr(shift) and 0xFF) + (color2.shr(shift) and 0xFF) * (color2 shr 24).toFloat() / 0xFF).toInt().coerceIn(0..0xFF).shl(shift)

    fun colorWithElevationOverlay(@ColorInt color: Int, @ColorInt colorOnSurface: Int, @Dimension(unit = Dimension.DP) elevation: Int): Int {
        val overlayAlpha =
            when (elevation) {
                0 -> 0f
                1 -> 0.05f
                2 -> 0.07f
                3 -> 0.08f
                4 -> 0.09f
                6 -> 0.11f
                8 -> 0.12f
                12 -> 0.14f
                16 -> 0.15f
                24 -> 0.16f
                else -> throw IllegalArgumentException("Unsupported elevation = $elevation dp")
            }

        return add(color, colorWithAlpha(colorOnSurface, overlayAlpha))
    }
}