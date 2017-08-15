package org.stepic.droid.ui.util

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.view_centered_toolbar.*


//Fragment's functions:

fun Fragment.initCenteredToolbar(@StringRes titleRes: Int, showHomeButton: Boolean = false) {
    initCenteredToolbarBase(showHomeButton)
    centeredToolbarTitle.setText(titleRes)
}

fun Fragment.initCenteredToolbar(title: String, showHomeButton: Boolean = false) {
    initCenteredToolbarBase(showHomeButton)
    centeredToolbarTitle.text = title
}

private fun Fragment.initCenteredToolbarBase(showHomeButton: Boolean) {
    val appCompatActivity = activity as AppCompatActivity
    appCompatActivity.initCenteredToolbarBase(showHomeButton)
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
                                                      @DrawableRes homeIndicator: Int = -1) {
    this.setSupportActionBar(centeredToolbar)

    val actionBar = this.supportActionBar
            ?: throw IllegalStateException("support action bar should be set")

    //for preventing showing default title
    actionBar.setDisplayShowTitleEnabled(false)

    if (showHomeButton) {
        actionBar.setDisplayHomeAsUpEnabled(true)
    }

    if (homeIndicator != -1) {
        //is not default
        actionBar.setHomeAsUpIndicator(homeIndicator)
    }
}
