package org.stepik.android.view.base.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import org.stepik.android.view.base.ui.widget.attributes.TextAttributes

class LatexView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    var attributes = TextAttributes.fromAttributeSet(context, attrs)
}