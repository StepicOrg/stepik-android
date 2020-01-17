package org.stepik.android.view.latex.ui.widget

import android.content.Context
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.ui.util.inflate
import org.stepik.android.domain.latex.mapper.LatexTextMapper
import org.stepik.android.domain.latex.model.LatexData
import org.stepik.android.view.base.ui.extension.ExternalLinkWebViewClient
import org.stepik.android.view.latex.mapper.LatexWebViewMapper
import org.stepik.android.view.latex.model.TextAttributes
import javax.inject.Inject

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

    @Inject
    internal lateinit var latexTextMapper: LatexTextMapper

    @Inject
    internal lateinit var latexWebViewMapper: LatexWebViewMapper

    @Inject
    internal lateinit var screenManager: ScreenManager

    @IdRes
    private val textViewId: Int

    @IdRes
    private val webViewId: Int

    lateinit var textView: TextView
        private set

    lateinit var webView: LatexWebView
        private set

    var attributes = TextAttributes.fromAttributeSet(context, attrs)
        set(value) {
            field = value
            textView.setAttributes(value)
            webView.attributes = value
        }

    var latexData: LatexData? = null
        set(value) {
            textView.isVisible = value is LatexData.Text
            webView.isVisible = value is LatexData.Web

            if (field == value) return
            field = value

            if (value == null) return

            when (value) {
                is LatexData.Text ->
                    textView.text = value.text

                is LatexData.Web ->
                    webView.text = latexWebViewMapper.mapLatexData(value, webView.attributes)
            }
        }

    init {
        App.component().inject(this)

        val array = context.obtainStyledAttributes(attrs, R.styleable.LatexView)

        try {
            val textId = array.getResourceId(R.styleable.LatexView_textViewId, 0)
            if (textId == 0) {
                textViewId = R.id.latex_textview
                inflate(R.layout.layout_latex_textview, attachToRoot = true)
            } else {
                textViewId = textId
            }

            val webId = array.getResourceId(R.styleable.LatexView_webViewId, 0)
            if (webId == 0) {
                webViewId = R.id.latex_webview
                inflate(R.layout.layout_latex_webview, attachToRoot = true)
            } else {
                webViewId = webId
            }
        } finally {
            array.recycle()
        }
    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        when (child?.id) {
            textViewId -> {
                textView = child as TextView
                textView.setAttributes(attributes)
                textView.movementMethod = LinkMovementMethod.getInstance()
            }

            webViewId -> {
                webView = child as LatexWebView
                webView.attributes = attributes
            }
        }
        super.addView(child, index, params)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        webView.onImageClickListener = { screenManager.openImage(context, it) }
        webView.webViewClient = ExternalLinkWebViewClient(context)
    }

    override fun onDetachedFromWindow() {
        webView.onImageClickListener = null
        webView.webViewClient = null
        super.onDetachedFromWindow()
    }

    fun setText(text: String?) {
        latexData = text?.let(latexTextMapper::mapToLatexText)
    }
}