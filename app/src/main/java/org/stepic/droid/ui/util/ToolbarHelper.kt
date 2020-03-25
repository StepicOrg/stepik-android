package org.stepic.droid.ui.util

import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.view_centered_toolbar.*
import org.stepic.droid.R
import org.stepik.android.view.base.ui.extension.setTintList


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
            ?.setTintList(actionBar.themedContext, R.attr.colorControlNormal)

        actionBar.setHomeAsUpIndicator(homeIndicatorDrawable)
    }
}

fun Toolbar.setTintedNavigationIcon(@DrawableRes iconRes: Int, @AttrRes tintRes: Int = R.attr.colorControlNormal) {
    this.navigationIcon = AppCompatResources
        .getDrawable(context, iconRes)
        ?.setTintList(context, tintRes)
}
