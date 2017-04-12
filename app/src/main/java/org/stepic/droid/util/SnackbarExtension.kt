package org.stepic.droid.util


import android.support.annotation.ColorInt
import android.support.design.widget.Snackbar
import android.widget.TextView

fun Snackbar.setTextColor(@ColorInt textColor: Int): Snackbar {
    val tv = this.view.findViewById(android.support.design.R.id.snackbar_text) as TextView
    tv.setTextColor(textColor)
    return this
}


val duration5Sec = 5000