package org.stepik.android.view.profile.ui.animation

import android.animation.ArgbEvaluator
import android.content.res.ColorStateList
import androidx.annotation.ColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import org.stepic.droid.databinding.FragmentProfileBinding
import org.stepik.android.view.base.ui.extension.ColorExtensions

class ProfileHeaderAnimationDelegate(
    profileBinding: FragmentProfileBinding,
    @ColorInt
    private val menuColorStart: Int,
    @ColorInt
    private val menuColorEnd: Int,
    @ColorInt
    private val toolbarColor: Int,

    private val onMenuColorChanged: (ColorStateList) -> Unit
) {
    private val profileCover = profileBinding.header.profileCover
    private val profileImage = profileBinding.header.profileImage

    private val toolbarTitle = profileBinding.toolbarTitle
    private val toolbarSeparator = profileBinding.toolbarSeparator.root

    private val appbar = profileBinding.appbar
    private val header = profileBinding.header.root

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
