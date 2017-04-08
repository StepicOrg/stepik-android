package org.stepic.droid.ui.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.common.api.GoogleApiClient
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.panel_custom_action_bar.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.analytic.LoginInteractionType
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.core.LoginFailType
import org.stepic.droid.core.ProgressHandler
import org.stepic.droid.core.presenters.LoginPresenter
import org.stepic.droid.core.presenters.contracts.LoginView
import org.stepic.droid.model.AuthData
import org.stepic.droid.ui.dialogs.LoadingProgressDialog
import org.stepic.droid.util.ProgressHelper
import org.stepic.droid.util.getMessageFor
import org.stepic.droid.util.toBundle
import javax.inject.Inject

class LoginActivity : FragmentActivityBase(), LoginView {

    companion object {
        private val TAG = "LoginActivity"
        private val RC_SAVE = 356
    }

    private val termsMessageHtml by lazy {
        resources.getString(R.string.terms_message_login)
    }

    private var progressLogin: ProgressDialog? = null

    private lateinit var progressHandler: ProgressHandler

    @Inject
    lateinit var loginPresenter: LoginPresenter

    private var googleApiClient: GoogleApiClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(null)
        setContentView(R.layout.activity_login)
        overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.no_transition)
        hideSoftKeypad()
        App.getComponentManager().loginComponent(TAG).inject(this)

        progressLogin = LoadingProgressDialog(this)
        progressHandler = object : ProgressHandler {
            override fun activate() {
                hideSoftKeypad()
                ProgressHelper.activate(progressLogin)
            }

            override fun dismiss() {
                ProgressHelper.dismiss(progressLogin)
            }
        }

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
                analytic.reportEvent(Analytic.Login.REQUEST_LOGIN_WITH_INTERACTION_TYPE, LoginInteractionType.ime.toBundle())
                tryLogin()
                handled = true
            }
            handled
        }

        val onFocusField = { _: View, hasFocus: Boolean ->
            if (hasFocus) {
                analytic.reportEvent(Analytic.Login.TAP_ON_FIELDS)
            }
        }
        loginText.setOnFocusChangeListener(onFocusField)
        passwordEditText.setOnFocusChangeListener(onFocusField)

        val reportAnalyticWhenTextBecomeNotBlank = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (s.isNullOrBlank()) {
                    analytic.reportEvent(Analytic.Login.TYPING_TEXT_FIELDS)
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }
        loginText.addTextChangedListener(reportAnalyticWhenTextBecomeNotBlank)
        passwordEditText.addTextChangedListener(reportAnalyticWhenTextBecomeNotBlank)


        actionbarCloseButtonLayout.setOnClickListener { finish() }
        loginButton.setOnClickListener {
            analytic.reportEvent(Analytic.Interaction.CLICK_SIGN_IN_ON_SIGN_IN_SCREEN)
            analytic.reportEvent(Analytic.Login.REQUEST_LOGIN_WITH_INTERACTION_TYPE, LoginInteractionType.button.toBundle())
            tryLogin()
        }

        loginRootView.requestFocus()

        if (checkPlayServices()) {
            googleApiClient = GoogleApiClient.Builder(this)
                    .enableAutoManage(this)
                    {}
                    .addApi(Auth.CREDENTIALS_API)
                    .build()
        }

        loginPresenter.attachView(this)
        onNewIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        //if we redirect from social:
        intent?.data?.let {
            redirectFromSocial(intent)
        }
    }

    private fun redirectFromSocial(intent: Intent) {
        try {
            val code = intent.data.getQueryParameter("code")
            loginPresenter.loginWithCode(code)
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

        loginPresenter.login(login, password)
    }

    override fun onDestroy() {
        loginPresenter.detachView(this)
        actionbarCloseButtonLayout.setOnClickListener(null)
        loginButton.setOnClickListener(null)
        if (isFinishing) {
            App.getComponentManager().releaseLoginComponent(TAG)
        }
        super.onDestroy()
    }

    override fun onFailLogin(type: LoginFailType, credential: Credential?) {
        Toast.makeText(this, getMessageFor(type), Toast.LENGTH_SHORT).show()
        progressHandler.dismiss()
    }

    override fun onSuccessLogin(authData: AuthData?) {
        progressHandler.dismiss()
        if (authData == null || !checkPlayServices() || !(googleApiClient?.isConnected ?: false)) {
            openMainFeed()
        } else {
            //only if we have not null data (we can apply smart lock && google api client is connected and available
            requestToSaveCredentials(authData)
        }
    }

    private fun requestToSaveCredentials(authData: AuthData) {
        val credential = Credential
                .Builder(authData.login)
                .setPassword(authData.password)
                .build()

        Auth.CredentialsApi.save(googleApiClient, credential)
                .setResultCallback { status ->
                    if (!status.isSuccess && status.hasResolution()) {
                        analytic.reportEvent(Analytic.SmartLock.SHOW_SAVE_LOGIN)
                        status.startResolutionForResult(this, RC_SAVE)
                    } else {
                        analytic.reportEventWithName(Analytic.SmartLock.DISABLED_LOGIN, status.statusMessage)
                        openMainFeed()
                    }
                }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SAVE) {
            if (resultCode == RESULT_OK) {
                analytic.reportEvent(Analytic.SmartLock.
                        LOGIN_SAVED)
            } else {
                analytic.reportEvent(Analytic.SmartLock.LOGIN_NOT_SAVED)
            }
            openMainFeed();
        }

    }

    private fun openMainFeed() {
        shell.screenProvider.showMainFeed(this, courseFromExtra)
    }

    override fun onLoadingWhileLogin() {
        progressHandler.activate()
    }

}
