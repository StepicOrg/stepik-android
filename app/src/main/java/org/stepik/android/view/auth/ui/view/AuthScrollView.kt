package org.stepik.android.view.auth.ui.view

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.widget.ScrollView

class AuthScrollView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ScrollView(context, attrs, defStyleAttr) {
    var isFocusScrollEnabled = true

    override fun computeScrollDeltaToGetChildRectOnScreen(rect: Rect): Int =
        if (isFocusScrollEnabled) {
            super.computeScrollDeltaToGetChildRectOnScreen(rect)
        } else {
            0
        }
}
