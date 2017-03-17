package org.stepic.droid.ui.activities

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.inputmethod.EditorInfo
import butterknife.ButterKnife
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.panel_custom_action_bar.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.core.ActivityFinisher
import org.stepic.droid.core.ProgressHandler
import org.stepic.droid.util.ProgressHelper

@SuppressLint("GoogleAppIndexingApiWarning")
class LoginActivity : FragmentActivityBase() {

    private lateinit var termsMessageHtml: String

    private var progressLogin: ProgressDialog? = null

    private val progressHandler = object : ProgressHandler {
        override fun activate() {
            hideSoftKeypad()
            ProgressHelper.activate(progressLogin)
        }

        override fun dismiss() {
            ProgressHelper.dismiss(progressLogin)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(null)
        setContentView(R.layout.activity_login)
        unbinder = ButterKnife.bind(this)
        overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.no_transition)
        hideSoftKeypad()

        termsMessageHtml = resources.getString(R.string.terms_message_login)
        termsPrivacyLogin.movementMethod = LinkMovementMethod.getInstance()
        termsPrivacyLogin.text = textResolver.fromHtml(termsMessageHtml)
        forgotPasswordView.setOnClickListener {
            shell.screenProvider.openRemindPassword(this@LoginActivity)
        }

        loginText.setOnEditorActionListener { _, actionId, _ ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                passwordEditText.requestFocus()
                handled = true
            }
            handled
        }

        passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                analytic.reportEvent(Analytic.Interaction.CLICK_SIGN_IN_NEXT_ON_SIGN_IN_SCREEN)
                tryLogin()
                handled = true
            }
            handled
        }

        actionbarCloseButtonLayout.setOnClickListener { finish() }
        loginButton.setOnClickListener {
            analytic.reportEvent(Analytic.Interaction.CLICK_SIGN_IN_ON_SIGN_IN_SCREEN)
            tryLogin()
        }

        loginRootView.requestFocus()

        //if we redirect from social:
        intent?.data?.let {
            redirectFromSocial(intent)
        }
    }

    private fun redirectFromSocial(intent: Intent) {
        try {
            val code = intent.data.getQueryParameter("code")
            loginManager.loginWithCode(code, progressHandler, object : ActivityFinisher {
                override fun onFinish() {
                    finish()
                }
            }, courseFromExtra)
        } catch (throwable: Throwable) {
            analytic.reportError(Analytic.Error.CALLBACK_SOCIAL, throwable)
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.no_transition, R.anim.slide_out_to_bottom)
    }

    private fun tryLogin() {
        val login = loginText.text.toString()
        val password = passwordEditText.text.toString()

        loginManager.login(login, password,
                progressHandler,
                object : ActivityFinisher {
                    override fun onFinish() {
                        finish()
                    }
                }, courseFromExtra)
    }

    override fun onDestroy() {
        actionbarCloseButtonLayout.setOnClickListener(null)
        loginButton.setOnClickListener(null)
        super.onDestroy()
    }

}
