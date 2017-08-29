package org.stepic.droid.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.activity_social_auth.*
import kotlinx.android.synthetic.main.panel_custom_action_bar.*
import kotlinx.android.synthetic.main.progress_bar_on_empty_screen.*
import org.stepic.droid.R
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.util.AppConstants


class SocialAuthActivity  : FragmentActivityBase() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_social_auth)
        overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.no_transition)


        loadProgressbarOnEmptyScreen.visibility = View.VISIBLE
        socialAuthWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url != null) {
                    val uri = Uri.parse(url)
                    if (url.startsWith(AppConstants.APP_SCHEME)) {
                        this@SocialAuthActivity.setResult(RESULT_OK, Intent().setData(uri))
                        this@SocialAuthActivity.finish()

                    } else if (uri.getQueryParameter(AppConstants.QUERY_ERROR) == AppConstants.ERROR_SOCIAL_AUTH_WITH_EXISTING_EMAIL
                            && uri.getQueryParameter(AppConstants.KEY_EMAIL_BUNDLE) != null) {

                        this@SocialAuthActivity.setResult(RESULT_CANCELED, Intent().setData(uri))
                        this@SocialAuthActivity.finish()
                    }
                }
                return false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                socialAuthWebView.visibility = View.VISIBLE
                loadProgressbarOnEmptyScreen.visibility = View.GONE
            }
        }
        socialAuthWebView.settings.javaScriptEnabled = true

        if (savedInstanceState == null) {
            val authUrl = intent.data.toString()
            socialAuthWebView.loadUrl(authUrl)
        } else {
            socialAuthWebView.restoreState(savedInstanceState)
        }

        actionbarCloseButtonLayout.setOnClickListener { finish() }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.no_transition, R.anim.slide_out_to_bottom)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        socialAuthWebView.saveState(outState)
    }

    override fun onBackPressed() {
        if (socialAuthWebView.canGoBack()) {
            socialAuthWebView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}