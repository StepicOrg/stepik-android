package org.stepic.droid.ui.util

import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.view_centered_toolbar.*


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
    appCompatActivity.setSupportActionBar(centeredToolbar)

    val actionBar = appCompatActivity.supportActionBar
            ?: throw IllegalStateException("support action bar should be set")

    //for preventing showing default title
    actionBar.setDisplayShowTitleEnabled(false)

    if (showHomeButton) {
        actionBar.setHomeButtonEnabled(true)
    }
}

fun Fragment.setTitleToCenteredToolbar(title: String) {
    centeredToolbarTitle.text = title
}