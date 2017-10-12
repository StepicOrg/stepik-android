package org.stepic.droid.ui.util

import android.graphics.Rect
import android.view.View
import timber.log.Timber

inline fun setOnKeyboardOpenListener(
        rootView: View,
        crossinline onKeyboardShown: () -> Unit,
        crossinline onKeyboardHidden: () -> Unit) {
    rootView.viewTreeObserver.addOnGlobalLayoutListener {
        val rect = Rect()
        rootView.getWindowVisibleDisplayFrame(rect)

        val screenHeight = rootView.rootView.height
        val keyboardHeight = screenHeight - rect.bottom

        Timber.d("keyboardHeight = $keyboardHeight")

        if (keyboardHeight > screenHeight * 0.15) {
            onKeyboardShown()
        } else {
            onKeyboardHidden()
        }
    }
}