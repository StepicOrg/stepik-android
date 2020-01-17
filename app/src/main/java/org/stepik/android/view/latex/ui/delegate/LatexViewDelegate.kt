package org.stepik.android.view.latex.ui.delegate

import android.content.Context
import androidx.core.view.isVisible
import org.stepic.droid.core.ScreenManager
import org.stepik.android.domain.latex.mapper.LatexTextMapper
import org.stepik.android.domain.latex.model.LatexData
import org.stepik.android.view.latex.mapper.LatexWebViewMapper
import org.stepik.android.view.latex.ui.widget.LatexView

/**
 * [context] - activity context, passed as separate variable as latexView context could be application context
 */
class LatexViewDelegate(
    private val context: Context,
    private val latexView: LatexView,

    private val latexTextMapper: LatexTextMapper,
    private val latexWebViewMapper: LatexWebViewMapper,
    private val screenManager: ScreenManager
) {
    private var latexData: LatexData? = null
        set(value) {
            if (field == value) return
            field = value

            latexView.textView.isVisible = latexData is LatexData.Text
            latexView.webView.isVisible = latexData is LatexData.Web
            if (value == null) return

            when (value) {
                is LatexData.Text ->
                    latexView.textView.text = value.text

                is LatexData.Web ->
                    latexView.webView.text = latexWebViewMapper.mapLatexData(value, latexView.webView.attributes, latexView.webView.width)
            }
        }

    fun setText(text: String?) {
        latexData = text?.let(latexTextMapper::mapToLatexText)
    }

    fun onAttach() {
        latexView.webView.onImageClickListener = { screenManager.openImage(context, it) }
    }

    fun onDetach() {
        latexView.webView.onImageClickListener = null
    }
}