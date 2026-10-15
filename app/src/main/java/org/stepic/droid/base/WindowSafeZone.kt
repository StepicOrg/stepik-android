package org.stepic.droid.base

import android.os.Build
import android.view.View
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import org.stepic.droid.R
import java.util.WeakHashMap

object WindowSafeZone {
    private const val DARK_ICON_LUMINANCE_THRESHOLD = 0.179
    private val installedViews = WeakHashMap<View, Boolean>()

    fun applyFullscreenDialog(window: Window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            val background = ContextCompat.getColor(window.context, R.color.color_background)
            val useDarkIcons = ColorUtils.calculateLuminance(background) > DARK_ICON_LUMINANCE_THRESHOLD
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.isAppearanceLightStatusBars = useDarkIcons
            controller.isAppearanceLightNavigationBars = useDarkIcons
        }
        apply(window)
    }

    fun apply(window: Window, onInsets: (Insets) -> Unit = {}) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return

        val content = window.findViewById<View>(android.R.id.content) ?: return
        if (installedViews.put(content, true) != null) return

        val initialLeft = content.paddingLeft
        val initialTop = content.paddingTop
        val initialRight = content.paddingRight
        val initialBottom = content.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(content) { view, insets ->
            val safeInsets = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            onInsets(safeInsets)
            view.setPadding(
                initialLeft + safeInsets.left,
                initialTop + safeInsets.top,
                initialRight + safeInsets.right,
                initialBottom + safeInsets.bottom
            )
            insets
        }
        if (ViewCompat.isAttachedToWindow(content)) {
            ViewCompat.requestApplyInsets(content)
        }
    }
}
