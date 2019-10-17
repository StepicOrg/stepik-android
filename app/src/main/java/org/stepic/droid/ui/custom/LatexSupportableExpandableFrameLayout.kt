package org.stepic.droid.ui.custom

import android.content.Context
import android.util.AttributeSet
import org.jetbrains.annotations.Contract
import org.stepic.droid.R

class LatexSupportableExpandableFrameLayout
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null): LatexSupportableEnhancedFrameLayout(context, attrs) {
    @Contract(pure = true)
    override fun getViewRes() = R.layout.latex_supportable_expandable_view

    @Contract(pure = true)
    override fun getTextViewId() = R.id.expandableTextView

    @Contract(pure = true)
    override fun getWebViewId() = R.id.htmlView
}