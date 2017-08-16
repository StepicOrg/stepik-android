package org.stepic.droid.ui.util

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.view_centered_toolbar.*
import org.stepic.droid.R


//Fragment's functions:

@JvmOverloads
fun Fragment.initCenteredToolbar(@StringRes titleRes: Int,
                                 showHomeButton: Boolean = false,
                                 @DrawableRes homeIndicatorRes: Int = -1) {
    initCenteredToolbarBase(showHomeButton, homeIndicatorRes)
    centeredToolbarTitle.setText(titleRes)
}

fun Fragment.initCenteredToolbar(title: String, showHomeButton: Boolean = false) {
    initCenteredToolbarBase(showHomeButton)
    centeredToolbarTitle.text = title
}

private fun Fragment.initCenteredToolbarBase(showHomeButton: Boolean,
                                             @DrawableRes homeIndicatorRes: Int = -1) {
    val appCompatActivity = activity as AppCompatActivity
    appCompatActivity.initCenteredToolbarBase(showHomeButton, homeIndicatorRes)
}

fun Fragment.setTitleToCenteredToolbar(title: String) {
    centeredToolbarTitle.text = title
}

//Activity's functions:

fun AppCompatActivity.initCenteredToolbar(@StringRes titleRes: Int,
                                          showHomeButton: Boolean = false,
                                          @DrawableRes homeIndicator: Int = -1) {
    initCenteredToolbarBase(showHomeButton, homeIndicator)
    centeredToolbarTitle.setText(titleRes)
}

private fun AppCompatActivity.initCenteredToolbarBase(showHomeButton: Boolean,
                                                      @DrawableRes homeIndicatorRes: Int = -1) {
    this.setSupportActionBar(centeredToolbar)

    val actionBar = this.supportActionBar
            ?: throw IllegalStateException("support action bar should be set")

    //for preventing showing default title
    actionBar.setDisplayShowTitleEnabled(false)

    if (showHomeButton) {
        actionBar.setDisplayHomeAsUpEnabled(true)
    }

    if (homeIndicatorRes > 0) {
        //is not default
        actionBar.setHomeAsUpIndicator(homeIndicatorRes)
    } else {
        //default
        @SuppressLint("PrivateResource")
        val defaultDrawable = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material)
        defaultDrawable.setColorFilter(ContextCompat.getColor(this, R.color.new_accent_color), PorterDuff.Mode.SRC_ATOP);
        actionBar.setHomeAsUpIndicator(defaultDrawable)
    }
}
