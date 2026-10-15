package org.stepic.droid.base

import android.os.Build
import android.view.View
import android.view.Window
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.WeakHashMap

object WindowSafeZone {
    private val installedViews = WeakHashMap<View, Boolean>()

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
