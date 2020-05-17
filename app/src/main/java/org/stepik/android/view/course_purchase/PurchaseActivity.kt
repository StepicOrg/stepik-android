package org.stepik.android.view.course_purchase

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebView
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_purchase.*
import kotlinx.android.synthetic.main.progress_bar_on_empty_screen.*
import org.stepic.droid.R
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepik.android.view.base.ui.extension.ExternalLinkWebViewClient

class PurchaseActivity : FragmentActivityBase() {
    companion object {
        private const val EXTRA_URL = "url"

        fun createIntent(context: Context, url: String): Intent =
            Intent(context, PurchaseActivity::class.java)
                .putExtra(EXTRA_URL, url)
    }

    private lateinit var url: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase)
        initCenteredToolbar(R.string.course_purchase, showHomeButton = true, homeIndicator = closeIconDrawableRes)
        loadProgressbarOnEmptyScreen.isVisible = true

        url = intent.getStringExtra(EXTRA_URL)
        @SuppressLint("SetJavaScriptEnabled")
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : ExternalLinkWebViewClient(this) {
            override fun onPageFinished(view: WebView?, url: String?) {
                loadProgressbarOnEmptyScreen.isVisible = false
                webView.isVisible = true
            }
        }
        webView.loadUrl(url)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean =
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else ->
                super.onOptionsItemSelected(item)
        }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.no_transition, R.anim.push_down)
    }
}