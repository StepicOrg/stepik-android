package org.stepic.droid.util

import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import org.stepic.droid.R

fun Fragment.snackbar(@StringRes messageRes: Int, length: Int = Snackbar.LENGTH_SHORT) {
    snackbar(getString(messageRes), length)
}

fun Fragment.snackbar(message: String, length: Int = Snackbar.LENGTH_SHORT) {
    val view = view
        ?: return

    Snackbar
        .make(view, message, length)
        .setTextColor(ContextCompat.getColor(view.context, R.color.white))
        .show()
}