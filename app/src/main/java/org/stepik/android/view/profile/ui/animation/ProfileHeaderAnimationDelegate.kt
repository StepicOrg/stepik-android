package org.stepik.android.view.profile.ui.animation

import android.animation.ArgbEvaluator
import android.content.res.ColorStateList
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import com.google.android.material.appbar.AppBarLayout
import org.stepic.droid.databinding.HeaderProfileBinding
import org.stepik.android.view.base.ui.extension.ColorExtensions

class ProfileHeaderAnimationDelegate(
    private val headerBinding: HeaderProfileBinding,
    private val appbar: AppBarLayout,
    private val toolbarTitle: TextView,
    private val toolbarSeparator: View,
    @ColorInt
    private val menuColorStart: Int,
    @ColorInt
    private val menuColorEnd: Int,
    @ColorInt
    private val toolbarColor: Int,

    private val onMenuColorChanged: (ColorStateList) -> Unit
) {
    private val argbEvaluator = ArgbEvaluator()

    fun onScroll(scrollY: Int) {
        val coverHeight = headerBinding.profileCover.height
        val toolbarHeight = appbar.height
        val headerHeight = headerBinding.root.height

        val coverScrollPercent = ((scrollY + 1f) / (coverHeight - toolbarHeight).coerceAtLeast(1))
            .coerceIn(0f, 1f)

        val toolbarBackground = ColorExtensions.colorWithAlpha(toolbarColor, coverScrollPercent)
        appbar.setBackgroundColor(toolbarBackground)

        val menuColor = argbEvaluator.evaluate(coverScrollPercent, menuColorStart, menuColorEnd) as Int
        onMenuColorChanged(ColorStateList.valueOf(menuColor))

        ViewCompat.setElevation(appbar, if (scrollY > headerHeight - toolbarHeight) ViewCompat.getElevation(headerBinding.root) else 0f)

        val scroll = (scrollY - headerBinding.profileImage.top).coerceAtMost(0).toFloat()
        toolbarTitle.translationY = -scroll

        val separatorBound = coverHeight.takeIf { it > 0 } ?: (headerBinding.profileImage.top + 1)
        toolbarSeparator.isVisible = (scrollY + toolbarHeight) in separatorBound until headerHeight
    }
}
