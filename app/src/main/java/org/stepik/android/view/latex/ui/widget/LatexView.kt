package org.stepik.android.view.latex.ui.widget

import android.content.Context
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.annotation.Px
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.TextViewCompat
import kotlinx.android.synthetic.main.latex_supportabe_enhanced_view.view.*
import org.stepic.droid.R
import org.stepic.droid.ui.util.inflate
import org.stepik.android.view.latex.model.TextAttributes

class LatexView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    companion object {
        private fun TextView.setAttributes(textAttributes: TextAttributes) {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, textAttributes.textSize)
            setTextColor(textAttributes.textColor)
            highlightColor = textAttributes.textColorHighlight
            setTextIsSelectable(textAttributes.textIsSelectable)
            typeface = ResourcesCompat.getFont(context, textAttributes.fontResId)
        }
    }

    var attributes = TextAttributes.fromAttributeSet(context, attrs)
        set(value) {
            field = value
            textView.setAttributes(value)
            webView.attributes = value
        }

    @LayoutRes
    private val layoutRes = R.layout.latex_supportabe_enhanced_view

    private val textView: TextView
    private val webView: LatexWebView

    init {
        val view = this.inflate(layoutRes, attachToRoot = true)
        textView = view.textView
        textView.movementMethod = LinkMovementMethod.getInstance()
        webView = view.webView
    }

    fun setLineHeight(@Px lineHeight: Int) {
        TextViewCompat.setLineHeight(textView, lineHeight)
    }


}