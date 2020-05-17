package org.stepik.android.view.course_purchase

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import kotlinx.android.synthetic.main.activity_course_purchase.*
import kotlinx.android.synthetic.main.error_no_connection_with_button.*
import kotlinx.android.synthetic.main.progress_bar_on_empty_screen.*
import org.stepic.droid.R
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepik.android.presentation.course_purchase.CoursePurchaseView
import org.stepik.android.view.base.ui.extension.ExternalLinkWebViewClient
import org.stepik.android.view.ui.delegate.ViewStateDelegate

class CoursePurchaseActivity : FragmentActivityBase(), CoursePurchaseView {
    companion object {
        private const val EXTRA_URL = "url"

        fun createIntent(context: Context, url: String): Intent =
            Intent(context, CoursePurchaseActivity::class.java)
                .putExtra(EXTRA_URL, url)
    }

    private lateinit var url: String

    private val viewStateDelegate = ViewStateDelegate<CoursePurchaseView.State>()
    private var state: CoursePurchaseView.State = CoursePurchaseView.State.Idle
        set(value) {
            field = value
            viewStateDelegate.switchState(value)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_purchase)
        initCenteredToolbar(R.string.course_purchase, showHomeButton = true, homeIndicator = closeIconDrawableRes)
        initViewStateDelegate()

        url = intent.getStringExtra(EXTRA_URL)

        @SuppressLint("SetJavaScriptEnabled")
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : ExternalLinkWebViewClient(this) {
            override fun onPageFinished(view: WebView?, url: String?) {
                if (state is CoursePurchaseView.State.Loading) {
                    state = CoursePurchaseView.State.Success
                }
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                state = CoursePurchaseView.State.Error
            }
        }

        state = CoursePurchaseView.State.Loading
        webView.loadUrl(url)

        tryAgain.setOnClickListener {
            if (state !is CoursePurchaseView.State.Error) return@setOnClickListener
            state = CoursePurchaseView.State.Loading
            webView.loadUrl(url)
        }
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

    private fun initViewStateDelegate() {
        viewStateDelegate.addState<CoursePurchaseView.State.Idle>()
        viewStateDelegate.addState<CoursePurchaseView.State.Loading>(loadProgressbarOnEmptyScreen)
        viewStateDelegate.addState<CoursePurchaseView.State.Error>(error)
        viewStateDelegate.addState<CoursePurchaseView.State.Success>(webView)
    }
}