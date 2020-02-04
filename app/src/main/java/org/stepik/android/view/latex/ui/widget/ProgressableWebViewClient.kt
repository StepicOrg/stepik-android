package org.stepik.android.view.latex.ui.widget

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.webkit.WebView
import androidx.core.view.isVisible
import org.stepik.android.view.base.ui.extension.ExternalLinkWebViewClient

class ProgressableWebViewClient(
    private val progressView: View,
    private val webView: View,
    context: Context = progressView.context
) : ExternalLinkWebViewClient(context) {
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        progressView.isVisible = true
        webView.isVisible = false
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        progressView.isVisible = false
        webView.isVisible = true
        super.onPageFinished(view, url)
    }
}