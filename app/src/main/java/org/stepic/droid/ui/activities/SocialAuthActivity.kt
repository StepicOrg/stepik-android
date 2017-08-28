package org.stepic.droid.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import org.stepic.droid.R
import org.stepic.droid.util.AppConstants


class SocialAuthActivity  : AppCompatActivity() {
    private lateinit var authWebView : WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_social_auth)
        overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.no_transition)

        val d = intent.data.toString()

        authWebView = findViewById(R.id.social_auth_web_view)
        authWebView.webViewClient = object : WebViewClient() {
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
        }
        authWebView.settings.javaScriptEnabled = true

        if (savedInstanceState == null) {
            authWebView.loadUrl(d)
        } else {
            authWebView.restoreState(savedInstanceState)
        }

        findViewById<View>(R.id.actionbarCloseButtonLayout).setOnClickListener { finish() }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.no_transition, R.anim.slide_out_to_bottom)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        authWebView.saveState(outState)
    }

    override fun onBackPressed() {
        if (authWebView.canGoBack()) {
            authWebView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}