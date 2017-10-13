package org.stepic.droid.ui.util

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager

inline fun setOnKeyboardOpenListener(
        rootView: View,
        crossinline onKeyboardShown: () -> Unit,
        crossinline onKeyboardHidden: () -> Unit) {
    rootView.viewTreeObserver.addOnGlobalLayoutListener {
        val rect = Rect()
        rootView.getWindowVisibleDisplayFrame(rect)

        val screenHeight = rootView.rootView.height
        val keyboardHeight = screenHeight - rect.bottom

        if (keyboardHeight > screenHeight * 0.15) {
            onKeyboardShown()
        } else {
            onKeyboardHidden()
        }
    }
}


fun listenKeyboardChanges(
        rootView: View,
        onKeyboardShown: () -> Unit,
        onKeyboardHidden: () -> Unit): ViewTreeObserver.OnGlobalLayoutListener {
    val metrics = DisplayMetrics()
    (rootView.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(metrics)
    val height = metrics.heightPixels
    val onGlobalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        val rect = Rect()
        rootView.getWindowVisibleDisplayFrame(rect)
        val keyboardHeight = height - rect.bottom
        if (keyboardHeight > height * 0.15) {
            onKeyboardShown()
        } else {
            onKeyboardHidden()
        }
    }
    rootView.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)
    return onGlobalLayoutListener
}

fun stopListenKeyboardChanges(
        rootView: View,
        onGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener?) {
    onGlobalLayoutListener?.let {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            rootView.viewTreeObserver.removeOnGlobalLayoutListener(it)
        } else {
            rootView.viewTreeObserver.removeGlobalOnLayoutListener(it)
        }
    }
}