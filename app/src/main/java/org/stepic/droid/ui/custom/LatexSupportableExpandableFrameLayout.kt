package org.stepic.droid.ui.custom

import android.util.AttributeSet
import android.content.Context
import org.stepic.droid.R

class LatexSupportableExpandableFrameLayout
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null): LatexSupportableEnhancedFrameLayout(context, attrs) {
    override fun getViewRes() = R.layout.latex_supportable_expandable_view
    override fun getTextViewId() = R.id.expandableTextView
    override fun getWebViewId() = R.id.htmlView
}