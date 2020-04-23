package org.stepik.android.view.profile.ui.animation

import android.animation.ArgbEvaluator
import android.content.res.ColorStateList
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.fragment_profile.view.*
import kotlinx.android.synthetic.main.header_profile.view.*
import org.stepik.android.view.base.ui.extension.ColorExtensions

class ProfileHeaderAnimationDelegate(
    view: View,
    @ColorInt
    private val menuColorStart: Int,
    @ColorInt
    private val menuColorEnd: Int,
    @ColorInt
    private val toolbarColor: Int,

    private val onMenuColorChanged: (ColorStateList) -> Unit
) {
    private val profileCover = view.profileCover
    private val profileImage = view.profileImage

    private val toolbarTitle = view.toolbarTitle
    private val toolbarSeparator = view.toolbarSeparator

    private val appbar = view.appbar
    private val header = view.header

    private val argbEvaluator = ArgbEvaluator()

    fun onScroll(scrollY: Int) {
        val coverHeight = profileCover.height
        val toolbarHeight = appbar.height
        val headerHeight =  header.height

        val coverScrollPercent = ((scrollY + 1f) / (coverHeight - toolbarHeight).coerceAtLeast(1))
            .coerceIn(0f, 1f)

        val toolbarBackground = ColorExtensions.colorWithAlpha(toolbarColor, coverScrollPercent)
        appbar.setBackgroundColor(toolbarBackground)

        val menuColor = argbEvaluator.evaluate(coverScrollPercent, menuColorStart, menuColorEnd) as Int
        onMenuColorChanged(ColorStateList.valueOf(menuColor))

        ViewCompat.setElevation(appbar, if (scrollY > headerHeight - toolbarHeight) ViewCompat.getElevation(header) else 0f)

        val scroll = (scrollY - profileImage.top).coerceAtMost(0).toFloat()
        toolbarTitle.translationY = -scroll

        val separatorBound = coverHeight.takeIf { it > 0 } ?: profileImage.top + 1
        toolbarSeparator.isVisible = (scrollY + toolbarHeight) in separatorBound until headerHeight
    }
}