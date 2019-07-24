package org.stepic.droid.ui.util

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager

const val PART_OF_KEYBOARD_ON_SCREEN = 0.15

//this method works good with activities, this listener will be destroyed with viewTree
inline fun setOnKeyboardOpenListener(
        rootView: View,
        crossinline onKeyboardShown: () -> Unit,
        crossinline onKeyboardHidden: () -> Unit) {
    rootView.viewTreeObserver.addOnGlobalLayoutListener {
        val rect = Rect()
        rootView.getWindowVisibleDisplayFrame(rect)

        val screenHeight = rootView.rootView.height
        val keyboardHeight = screenHeight - rect.bottom

        if (keyboardHeight > screenHeight * PART_OF_KEYBOARD_ON_SCREEN) {
            onKeyboardShown()
        } else {
            onKeyboardHidden()
        }
    }
}

//methods for retain fragments
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
        if (keyboardHeight > height * PART_OF_KEYBOARD_ON_SCREEN) {
            onKeyboardShown()
        } else {
            onKeyboardHidden()
        }
    }
    rootView.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)
    return onGlobalLayoutListener
}

//stop listening for avoiding memory leak
fun stopListenKeyboardChanges(
        rootView: View,
        onGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener?) {
    onGlobalLayoutListener?.let {
        rootView.viewTreeObserver.removeOnGlobalLayoutListener(it)
    }
}