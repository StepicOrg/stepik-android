package org.stepic.droid.ui.activities

import android.os.Bundle
import android.support.v7.app.AppCompatDelegate
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.google.android.gms.auth.api.credentials.Credential
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.activity_register.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.analytic.LoginInteractionType
import org.stepic.droid.base.App
import org.stepic.droid.core.LoginFailType
import org.stepic.droid.core.presenters.LoginPresenter
import org.stepic.droid.core.presenters.contracts.LoginView
import org.stepic.droid.fonts.FontType
import org.stepic.droid.model.Credentials
import org.stepic.droid.ui.dialogs.LoadingProgressDialog
import org.stepic.droid.ui.util.setOnKeyboardOpenListener
import org.stepic.droid.util.*
import org.stepic.droid.web.Api
import org.stepic.droid.web.RegistrationResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import uk.co.chrisjenx.calligraphy.CalligraphyTypefaceSpan
import uk.co.chrisjenx.calligraphy.TypefaceUtils
import javax.inject.Inject

class RegisterActivity : SmartLockActivityBase(), LoginView {
    companion object {
        const val ERROR_DELIMITER = " "
        const val TAG = "RegisterActivity"

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    private val passwordTooShortMessage by lazy {
        resources.getString(R.string.password_too_short)
    }
    private val termsMessageHtml by lazy {
        resources.getString(R.string.terms_message_register)
    }

    private lateinit var progressBar: LoadingProgressDialog

    @Inject
    lateinit var loginPresenter: LoginPresenter

    @Inject
    internal lateinit var retrofit: Retrofit

    @Inject
    internal lateinit var api: Api

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        App.componentManager().loginComponent(TAG).inject(this)

        initTitle()

        termsPrivacyRegisterTextView.movementMethod = LinkMovementMethod.getInstance()
        termsPrivacyRegisterTextView.text = textResolver.fromHtml(termsMessageHtml)
        stripUnderlinesFromLinks(termsPrivacyRegisterTextView)

        signUpButton.setOnClickListener { createAccount(LoginInteractionType.button) }

        progressBar = LoadingProgressDialog(this)

        passwordField.setOnEditorActionListener { _, actionId, _ ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                analytic.reportEvent(Analytic.Registration.CLICK_SEND_IME)
                createAccount(LoginInteractionType.ime)
                handled = true
            }
            handled
        }
        val reportAnalyticWhenTextBecomeNotBlank = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (s.isNullOrBlank()) {
                    analytic.reportEvent(Analytic.Registration.TYPING_TEXT_FIELDS)
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                onClearError()
                setSignUpButtonState()
            }
        }

        firstNameField.addTextChangedListener(reportAnalyticWhenTextBecomeNotBlank)
        emailField.addTextChangedListener(reportAnalyticWhenTextBecomeNotBlank)
        passwordField.addTextChangedListener(reportAnalyticWhenTextBecomeNotBlank)

        val onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            reportTap(hasFocus)
        }
        firstNameField.onFocusChangeListener = onFocusChangeListener
        emailField.onFocusChangeListener = onFocusChangeListener
        passwordField.onFocusChangeListener = onFocusChangeListener

        firstNameField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                emailField.requestFocus()
                true
            } else {
                false
            }
        }

        emailField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                passwordField.requestFocus()
                true
            } else {
                false
            }
        }

        registerRootView.requestFocus()

        initGoogleApiClient()

        setSignUpButtonState()

        setOnKeyboardOpenListener(root_view, {
            stepikLogo.visibility = View.GONE
            signUpText.visibility = View.GONE
        }, {
            stepikLogo.visibility = View.VISIBLE
            signUpText.visibility = View.VISIBLE
        })

        loginPresenter.attachView(this)
    }

    private fun setSignUpButtonState() {
        signUpButton.isEnabled = emailField.text.isNotBlank() && firstNameField.text.isNotBlank() && passwordField.text.isNotBlank()
    }

    private fun initTitle() {
        val signUpString = getString(R.string.sign_up)
        val signUpSuffix = getString(R.string.sign_up_with_email_suffix)

        val spannableSignIn = SpannableString(signUpString + signUpSuffix)
        val typefaceSpan = CalligraphyTypefaceSpan(TypefaceUtils.load(assets, fontsProvider.provideFontPath(FontType.medium)))

        spannableSignIn.setSpan(typefaceSpan, 0, signUpString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        signUpText.text = spannableSignIn
    }

    private fun reportTap(hasFocus: Boolean) {
        if (hasFocus) {
            analytic.reportEvent(Analytic.Registration.TAP_ON_FIELDS)
        }
    }


    private fun createAccount(interactionType: LoginInteractionType) {
        analytic.reportEvent(Analytic.Registration.CLICK_WITH_INTERACTION_TYPE, interactionType.toBundle())
        hideSoftKeypad()

        val firstName = firstNameField.text.toString().trim()
        val lastName = " " // registrationSecondName.text.toString().trim()
        val email = emailField.text.toString().trim()
        val password = passwordField.text.toString()

        analytic.reportEvent(Analytic.Interaction.CLICK_REGISTER_BUTTON)

        var isOk = true

        if (!ValidatorUtil.isPasswordValid(password)) {
            showError(passwordTooShortMessage)
            isOk = false
        }

        if (isOk) {
            onLoadingWhileLogin()
            api.signUp(firstName, lastName, email, password).enqueue(object : Callback<RegistrationResponse> {
                override fun onResponse(call: Call<RegistrationResponse>, response: Response<RegistrationResponse>) {
                    if (response.isSuccessful) {
                        analytic.reportEvent(FirebaseAnalytics.Event.SIGN_UP)

                        loginPresenter.login(email, password, isAfterRegistration = true)
                    } else {
                        ProgressHelper.dismiss(progressBar)
                        response.errorBody()
                        val errorConverter = retrofit.responseBodyConverter<RegistrationResponse>(RegistrationResponse::class.java, arrayOfNulls<Annotation>(0))
                        var error: RegistrationResponse? = null
                        try {
                            error = errorConverter.convert(response.errorBody()!!)
                        } catch (e: Exception) {
                            analytic.reportError(Analytic.Error.REGISTRATION_IMPORTANT_ERROR, e) //it is unknown response Expected BEGIN_OBJECT but was STRING at line 1 column 1 path
                        }

                        handleErrorRegistrationResponse(error)
                    }

                }

                override fun onFailure(call: Call<RegistrationResponse>, t: Throwable) {
                    ProgressHelper.dismiss(progressBar)
                    Toast.makeText(this@RegisterActivity, R.string.connectionProblems, Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun onClearError() {
        signUpButton.isEnabled = true
        registerForm.isEnabled = true
        registerErrorMessage.visibility = View.GONE
    }

    override fun onDestroy() {
        loginPresenter.detachView(this)
        passwordField.setOnEditorActionListener(null)
        if (isFinishing) {
            App.componentManager().releaseLoginComponent(TAG)
        }
        super.onDestroy()
    }

    override fun applyTransitionPrev() {} // we need default system animation

    private fun showError(errorText: String?) {
        errorText?.let {
            analytic.reportEventWithName(Analytic.Registration.ERROR, errorText)
            if (registerErrorMessage.visibility == View.GONE) {
                signUpButton.isEnabled = false
                registerForm.isEnabled = false
                registerErrorMessage.text = it
                registerErrorMessage.visibility = View.VISIBLE
            }
        }
    }

    private fun handleErrorRegistrationResponse(registrationResponse: RegistrationResponse?) {
        if (registrationResponse == null) return
        showError(getErrorString(registrationResponse.email))
        showError(getErrorString(registrationResponse.first_name))
        showError(getErrorString(registrationResponse.last_name))
        showError(getErrorString(registrationResponse.password))
    }

    private fun getErrorString(values: Array<String?>?): String? =
        values
            ?.takeIf { it.isNotEmpty() }
            ?.joinToString(separator = ERROR_DELIMITER)

    override fun onFailLogin(type: LoginFailType, credential: Credential?) {
        ProgressHelper.dismiss(progressBar)
        Toast.makeText(this, getMessageFor(type), Toast.LENGTH_SHORT).show()
    }

    override fun onSuccessLogin(credentials: Credentials?) {
        ProgressHelper.dismiss(progressBar)
        if (credentials == null || !checkPlayServices() || !(googleApiClient?.isConnected ?: false)) {
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
        hideSoftKeypad()
        ProgressHelper.activate(progressBar)
    }

    override fun onSocialLoginWithExistingEmail(email: String) {}

}
