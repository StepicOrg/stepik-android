package org.stepic.droid.ui.util

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.view_centered_toolbar.*
import org.stepic.droid.R
import org.stepic.droid.util.resolveAttribute


//Fragment's functions:

@JvmOverloads
fun Fragment.initCenteredToolbar(
    @StringRes titleRes: Int,
    showHomeButton: Boolean = false,
    @DrawableRes homeIndicatorRes: Int = -1
) {
    initCenteredToolbarBase(showHomeButton, homeIndicatorRes)
    centeredToolbarTitle.setText(titleRes)
}

@JvmOverloads
fun Fragment.initCenteredToolbar(
    title: String,
    showHomeButton: Boolean = false,
    @DrawableRes homeIndicatorRes: Int = -1
) {
    initCenteredToolbarBase(showHomeButton, homeIndicatorRes)
    centeredToolbarTitle.text = title
}

private fun Fragment.initCenteredToolbarBase(
    showHomeButton: Boolean,
    @DrawableRes homeIndicatorRes: Int = -1
) {
    val appCompatActivity = activity as AppCompatActivity
    appCompatActivity.initCenteredToolbarBase(showHomeButton, homeIndicatorRes)
}

fun Fragment.setTitleToCenteredToolbar(title: String) {
    centeredToolbarTitle.text = title
}

//Activity's functions:

fun AppCompatActivity.initCenteredToolbar(
    @StringRes titleRes: Int,
    showHomeButton: Boolean = false,
    @DrawableRes homeIndicator: Int = -1
) {
    initCenteredToolbarBase(showHomeButton, homeIndicator)
    centeredToolbarTitle.setText(titleRes)
}

private fun AppCompatActivity.initCenteredToolbarBase(
    showHomeButton: Boolean,
    @DrawableRes homeIndicatorRes: Int = -1
) {
    this.setSupportActionBar(centeredToolbar)

    val actionBar = this.supportActionBar
            ?: throw IllegalStateException("support action bar should be set")

    //for preventing showing default title
    actionBar.setDisplayShowTitleEnabled(false)

    if (showHomeButton) {
        actionBar.setDisplayHomeAsUpEnabled(true)
    }

    if (homeIndicatorRes != -1) {
        //is not default
        val homeIndicatorDrawable = AppCompatResources
            .getDrawable(actionBar.themedContext, homeIndicatorRes)
            ?.mutate()

        homeIndicatorDrawable
            ?.let { drawable ->
                val colorControlNormalRes = actionBar.themedContext.resolveAttribute(R.attr.colorControlNormal)?.resourceId ?: return@let
                val colorControlNormal = ContextCompat.getColorStateList(actionBar.themedContext, colorControlNormalRes)
                DrawableCompat.setTintList(drawable, colorControlNormal)
            }
        actionBar.setHomeAsUpIndicator(homeIndicatorDrawable)
    }
}
