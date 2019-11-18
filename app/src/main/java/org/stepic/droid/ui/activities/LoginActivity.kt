package org.stepic.droid.ui.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.auth.api.credentials.Credential
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.ResponseBody
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.analytic.LoginInteractionType
import org.stepic.droid.base.App
import org.stepic.droid.core.LoginFailType
import org.stepic.droid.core.ProgressHandler
import org.stepic.droid.core.presenters.LoginPresenter
import org.stepic.droid.core.presenters.contracts.LoginView
import org.stepic.droid.model.Credentials
import org.stepic.droid.ui.dialogs.LoadingProgressDialog
import org.stepic.droid.ui.util.setOnKeyboardOpenListener
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.ProgressHelper
import org.stepic.droid.util.getMessageFor
import org.stepic.droid.util.toBundle
import org.stepik.android.view.base.ui.span.TypefaceSpanCompat
import ru.nobird.android.view.base.ui.extension.hideKeyboard
import javax.inject.Inject

class LoginActivity : SmartLockActivityBase(), LoginView {

    companion object {
        private const val TAG = "LoginActivity"
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    private var progressLogin: LoadingProgressDialog? = null

    private lateinit var progressHandler: ProgressHandler

    @Inject
    lateinit var loginPresenter: LoginPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        App.componentManager().loginComponent(TAG).inject(this)

        progressLogin = LoadingProgressDialog(this)
        progressHandler = object : ProgressHandler {
            override fun activate() {
                currentFocus?.hideKeyboard()
                ProgressHelper.activate(progressLogin)
            }

            override fun dismiss() {
                ProgressHelper.dismiss(progressLogin)
            }
        }

        initTitle()

        forgotPasswordView.setOnClickListener {
            screenManager.openRemindPassword(this@LoginActivity)
        }

        loginField.setOnEditorActionListener { _, actionId, _ ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                passwordField.requestFocus()
                handled = true
            }
            handled
        }

        passwordField.setOnEditorActionListener { _, actionId, _ ->
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
        loginField.setOnFocusChangeListener(onFocusField)
        passwordField.setOnFocusChangeListener(onFocusField)

        val reportAnalyticWhenTextBecomeNotBlank = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (s.isNullOrBlank()) {
                    analytic.reportEvent(Analytic.Login.TYPING_TEXT_FIELDS)
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                onClearLoginError()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }
        loginField.addTextChangedListener(reportAnalyticWhenTextBecomeNotBlank)
        passwordField.addTextChangedListener(reportAnalyticWhenTextBecomeNotBlank)


        launchSignUpButton.setOnClickListener {
            analytic.reportEvent(Analytic.Interaction.CLICK_SIGN_UP)
            screenManager.showRegistration(this@LoginActivity, courseFromExtra)
        }

        signInWithSocial.setOnClickListener { finish() }
        loginButton.setOnClickListener {
            analytic.reportEvent(Analytic.Interaction.CLICK_SIGN_IN_ON_SIGN_IN_SCREEN)
            analytic.reportEvent(Analytic.Login.REQUEST_LOGIN_WITH_INTERACTION_TYPE, LoginInteractionType.button.toBundle())
            tryLogin()
        }

        loginRootView.requestFocus()

        initGoogleApiClient()

        onNewIntent(intent)

        if (savedInstanceState == null && intent.hasExtra(AppConstants.KEY_EMAIL_BUNDLE)) {
            loginField.setText(intent.getStringExtra(AppConstants.KEY_EMAIL_BUNDLE))
            passwordField.requestFocus()
        }

        setOnKeyboardOpenListener(root_view, {
            stepikLogo.visibility = View.GONE
            signInText.visibility = View.GONE
        }, {
            stepikLogo.visibility = View.VISIBLE
            signInText.visibility = View.VISIBLE
        })
    }

    private fun initTitle() {
        val signInString = getString(R.string.sign_in)
        val signInWithPasswordSuffix = getString(R.string.sign_in_with_password_suffix)

        val spannableSignIn = SpannableString(signInString + signInWithPasswordSuffix)
        val typeface = ResourcesCompat.getFont(this, R.font.roboto_medium)

        spannableSignIn.setSpan(TypefaceSpanCompat(typeface), 0, signInString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        signInText.text = spannableSignIn
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
            val code = intent?.data.getQueryParameter("code")
            loginPresenter.loginWithCode(code)
        } catch (throwable: Throwable) {
            analytic.reportError(Analytic.Error.CALLBACK_SOCIAL, throwable)
        }
    }

    private fun tryLogin() {
        val login = loginField.text.toString()
        val password = passwordField.text.toString()

        loginPresenter.login(login, password)
    }

    override fun applyTransitionPrev() {} // we need default system animation

    override fun onDestroy() {
        signInWithSocial.setOnClickListener(null)
        loginButton.setOnClickListener(null)
        launchSignUpButton.setOnClickListener(null)
        if (isFinishing) {
            App.componentManager().releaseLoginComponent(TAG)
        }
        super.onDestroy()
    }

    private fun onClearLoginError() {
        loginButton.isEnabled = true
        loginForm.isEnabled = true
        loginErrorMessage.visibility = View.GONE
    }

    override fun onFailLogin(type: LoginFailType, credential: Credential?) {
        loginErrorMessage.text = getMessageFor(type)
        loginErrorMessage.visibility = View.VISIBLE

        if (type == LoginFailType.EMAIL_ALREADY_USED || type == LoginFailType.EMAIL_PASSWORD_INVALID) {
            loginForm.isEnabled = false
            loginButton.isEnabled = false
        }

        progressHandler.dismiss()
    }

    override fun onSuccessLogin(credentials: Credentials?) {
        progressHandler.dismiss()
        if (credentials == null || !checkPlayServices() || googleApiClient?.isConnected != true) {
            openMainFeed()
        } else {
            //only if we have not null data (we can apply smart lock && google api client is connected and available
            requestToSaveCredentials(credentials)
        }
    }

    override fun onCredentialSaved() = openMainFeed()

    private fun openMainFeed() {
        screenManager.showMainFeedAfterLogin(this, courseFromExtra)
    }

    override fun onLoadingWhileLogin() {
        progressHandler.activate()
    }

    override fun onSocialLoginWithExistingEmail(email: String) {}

    override fun onResume() {
        super.onResume()
        loginPresenter.attachView(this)
    }

    override fun onPause() {
        loginPresenter.detachView(this)
        super.onPause()
    }

    override fun onRegistrationFailed(responseBody: ResponseBody?) {
        // no op
    }
}
