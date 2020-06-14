package org.stepik.android.view.base.ui.extension

import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import org.stepic.droid.R
import org.stepic.droid.util.resolveColorAttribute

object ColorExtensions {
    private const val MASK_OPACITY = 0xFF
    private const val MASK_NO_OPACITY = 0x00FFFFFF

    @ColorInt
    fun colorWithAlpha(@ColorInt color: Int, alpha: Float): Int =
        (alpha * MASK_OPACITY).toInt().shl(24) or (color and MASK_NO_OPACITY)

    /**
     * Multiplies current alpha channel with given [alpha]
     */
    @ColorInt
    fun colorWithAlphaMul(@ColorInt color: Int, alpha: Float): Int =
        (alpha * color.shr(24)).toInt().shl(24) or (color and MASK_NO_OPACITY)

    /**
     * Mixes [color1] with [color2] in additive way
     */
    @ColorInt
    fun add(@ColorInt color1: Int, @ColorInt color2: Int, overrideLightTheme: Boolean = false): Int =
        ((color1 shr 24) shl 24) or // alpha
                addChannel(0, color1, color2, overrideLightTheme) or // b
                addChannel(8, color1, color2, overrideLightTheme) or // g
                addChannel(16, color1, color2, overrideLightTheme) // r

    private fun addChannel(shift: Int, @ColorInt color1: Int, @ColorInt color2: Int, overrideLightTheme: Boolean = false): Int {
        val ch1 = color1.shr(shift) and 0xFF
        val ch2 = color2.shr(shift) and 0xFF
        val alpha = (color2 shr 24).toFloat() / 0xFF

        val ch =
            if (overrideLightTheme && ch1 > ch2) {
                0xFF - (0xFF - ch1) - (0xFF - ch2) * alpha
            } else {
                ch1 + ch2 * alpha
            }
        return ch.toInt().coerceIn(0..0xFF).shl(shift)
    }

    @ColorInt
    fun colorWithElevationOverlay(@ColorInt color: Int, @ColorInt colorOnSurface: Int, @Dimension(unit = Dimension.DP) elevation: Int, overrideLightTheme: Boolean = false): Int {
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

        return add(color, colorWithAlpha(colorOnSurface, overlayAlpha), overrideLightTheme)
    }

    /**
     * If [overrideLightTheme] is true light color will be darken otherwise light color wouldn't be changed
     */
    @ColorInt
    fun colorSurfaceWithElevationOverlay(context: Context, @Dimension(unit = Dimension.DP) elevation: Int, overrideLightTheme: Boolean = false): Int {
        val colorSurface = context.resolveColorAttribute(R.attr.colorSurface)
        val colorOnSurface = context.resolveColorAttribute(R.attr.colorOnSurface)

        return colorWithElevationOverlay(colorSurface, colorOnSurface, elevation, overrideLightTheme)
    }
}