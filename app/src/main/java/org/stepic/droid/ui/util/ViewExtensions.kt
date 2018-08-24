package org.stepic.droid.ui.util

import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver


fun ViewTreeObserver.removeGlobalLayoutListener(listener: ViewTreeObserver.OnGlobalLayoutListener) {
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
        removeOnGlobalLayoutListener(listener)
    } else {
        @Suppress("DEPRECATION") //use only on old API
        removeGlobalOnLayoutListener(listener)
    }
}

fun View.changeVisibility(needShow: Boolean) {
    if (needShow) {
        this.visibility = View.VISIBLE
    } else {
        this.visibility = View.GONE
    }
}

fun View.setHeight(height: Int) {
    layoutParams.height = height
    layoutParams = layoutParams
}

fun ViewGroup.hideAllChildren() {
    for (i in 0 until childCount) {
        getChildAt(i).changeVisibility(false)
    }
}