package org.stepik.android.view.latex.ui.delegate

import android.content.Context
import androidx.core.view.isVisible
import org.stepic.droid.core.ScreenManager
import org.stepik.android.domain.latex.mapper.LatexTextMapper
import org.stepik.android.domain.latex.model.LatexData
import org.stepik.android.view.base.ui.extension.ExternalLinkWebViewClient
import org.stepik.android.view.latex.mapper.LatexWebViewMapper
import org.stepik.android.view.latex.ui.widget.LatexView
import javax.inject.Inject

class LatexViewDelegate
@Inject
constructor(
    private val latexTextMapper: LatexTextMapper,
    private val latexWebViewMapper: LatexWebViewMapper,
    private val screenManager: ScreenManager
) {
    private var latexView: LatexView? = null

    private var latexData: LatexData? = null
        set(value) {
            if (field == value) return
            field = value

            val latexView = this.latexView ?: return
            setDataToView(latexView, value)
        }

    fun setText(text: String?) {
        latexData = text?.let(latexTextMapper::mapToLatexText)
    }

    private fun setDataToView(latexView: LatexView, latexData: LatexData?) {
        latexView.textView.isVisible = latexData is LatexData.Text
        latexView.webView.isVisible = latexData is LatexData.Web
        if (latexData == null) return

        when (latexData) {
            is LatexData.Text ->
                latexView.textView.text = latexData.text

            is LatexData.Web ->
                latexView.webView.text = latexWebViewMapper.mapLatexData(latexData, latexView.webView.attributes)
        }
    }

    /**
     * [context] - activity context, passed as separate variable as latexView context could be application context
     */
    fun attach(context: Context, latexView: LatexView) {
        this.latexView = latexView
        latexView.webView.onImageClickListener = { screenManager.openImage(context, it) }
        latexView.webView.webViewClient = ExternalLinkWebViewClient(context)
        setDataToView(latexView, latexData)
    }

    fun detach() {
        latexView?.webView?.apply {
            onImageClickListener = null
            webViewClient = null
        }
        latexView = null
    }
}