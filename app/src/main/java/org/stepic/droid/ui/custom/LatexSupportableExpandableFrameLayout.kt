package org.stepic.droid.ui.custom

import android.util.AttributeSet
import android.content.Context
import android.view.LayoutInflater
import kotlinx.android.synthetic.main.latex_supportable_expandable_view.view.*
import org.stepic.droid.R

class LatexSupportableExpandableFrameLayout
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null): LatexSupportableEnhancedFrameLayout(context, attrs) {
    override fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.latex_supportable_expandable_view, this, true)
        textView = expandableTextView
        webView = htmlView
    }
}