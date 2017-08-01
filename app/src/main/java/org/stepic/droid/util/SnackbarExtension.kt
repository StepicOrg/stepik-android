package org.stepic.droid.util


import android.support.annotation.ColorInt
import android.support.design.widget.Snackbar
import android.widget.TextView

fun Snackbar.setTextColor(@ColorInt textColor: Int): Snackbar {
    val tv = this.view.findViewById<TextView>(android.support.design.R.id.snackbar_text)
    tv.setTextColor(textColor)
    return this
}


val duration5Sec = 5000