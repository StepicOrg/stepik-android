package org.stepic.droid.ui.util

import android.view.View
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
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
    appCompatActivity.initCenteredToolbarBase(centeredToolbar, showHomeButton, homeIndicatorRes)
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
    initCenteredToolbarBase(centeredToolbar, showHomeButton, homeIndicator)
    centeredToolbarTitle.setText(titleRes)
}

private fun AppCompatActivity.initCenteredToolbarBase(
    toolbar: Toolbar,
    showHomeButton: Boolean,
    @DrawableRes homeIndicatorRes: Int = -1
) {
    this.setSupportActionBar(toolbar)

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


private val Fragment.centeredToolbar: Toolbar
    get() = requireView().findCenteredToolbar()

private val Fragment.centeredToolbarTitle: TextView
    get() = requireView().findCenteredToolbarTitle()

private val AppCompatActivity.centeredToolbar: Toolbar
    get() = findCenteredToolbar()

private val AppCompatActivity.centeredToolbarTitle: TextView
    get() = findCenteredToolbarTitle()

private fun View.findCenteredToolbar(): Toolbar =
    findViewById(R.id.centeredToolbar)
        ?: findCenteredToolbarTitle().findParentToolbar()
        ?: throw IllegalStateException("View with R.id.centeredToolbarTitle must be inside a Toolbar")

private fun View.findCenteredToolbarTitle(): TextView =
    findViewById(R.id.centeredToolbarTitle)
        ?: throw IllegalStateException("View with R.id.centeredToolbarTitle was not found")

private fun AppCompatActivity.findCenteredToolbar(): Toolbar =
    findViewById(R.id.centeredToolbar)
        ?: findCenteredToolbarTitle().findParentToolbar()
        ?: throw IllegalStateException("View with R.id.centeredToolbarTitle must be inside a Toolbar")

private fun AppCompatActivity.findCenteredToolbarTitle(): TextView =
    findViewById(R.id.centeredToolbarTitle)
        ?: throw IllegalStateException("View with R.id.centeredToolbarTitle was not found")

// An <include android:id="..."> overrides the included toolbar root id at runtime,
// so centeredToolbarTitle can be the only stable id. Walk up from it to recover the Toolbar.
private fun View.findParentToolbar(): Toolbar? {
    var currentParent = parent
    while (currentParent is View) {
        if (currentParent is Toolbar) {
            return currentParent
        }
        currentParent = (currentParent as View).parent
    }
    return null
}