package org.stepic.droid.ui.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.activity_register_new.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.analytic.LoginInteractionType
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.core.LoginFailType
import org.stepic.droid.core.presenters.LoginPresenter
import org.stepic.droid.core.presenters.contracts.LoginView
import org.stepic.droid.fonts.FontType
import org.stepic.droid.model.AuthData
import org.stepic.droid.util.*
import org.stepic.droid.web.RegistrationResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import uk.co.chrisjenx.calligraphy.CalligraphyTypefaceSpan
import uk.co.chrisjenx.calligraphy.TypefaceUtils
import javax.inject.Inject


class RegisterActivity : FragmentActivityBase(), LoginView {
    companion object {
        val ERROR_DELIMITER = " "
        val TAG = "RegisterActivity"
        private val RC_SAVE = 356
    }

    private val passwordTooShortMessage by lazy {
        resources.getString(R.string.password_too_short)
    }
    private val termsMessageHtml by lazy {
        resources.getString(R.string.terms_message_register)
    }

    private lateinit var progressBar: ProgressDialog

    @Inject
    lateinit var loginPresenter: LoginPresenter

    private var googleApiClient: GoogleApiClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(org.stepic.droid.R.layout.activity_register_new)
        overridePendingTransition(R.anim.slide_in_from_end, R.anim.slide_out_to_start)
        hideSoftKeypad()
        App.componentManager().loginComponent(TAG).inject(this)

        initTitle()

        termsPrivacyRegisterTextView.movementMethod = LinkMovementMethod.getInstance()
        termsPrivacyRegisterTextView.text = textResolver.fromHtml(termsMessageHtml)
        stripUnderlinesFromLinks(termsPrivacyRegisterTextView)

        signUpButton.setOnClickListener { createAccount(LoginInteractionType.button) }

        progressBar = ProgressDialog(this)
        progressBar.setTitle(getString(R.string.loading))
        progressBar.setMessage(getString(R.string.loading_message))
        progressBar.setCancelable(false)

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

        emailField.addTextChangedListener(reportAnalyticWhenTextBecomeNotBlank)
        firstNameField.addTextChangedListener(reportAnalyticWhenTextBecomeNotBlank)
        passwordField.addTextChangedListener(reportAnalyticWhenTextBecomeNotBlank)

        val onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            reportTap(hasFocus)
        }
        emailField.onFocusChangeListener = onFocusChangeListener
        firstNameField.onFocusChangeListener = onFocusChangeListener
        passwordField.onFocusChangeListener = onFocusChangeListener

        registerRootView.requestFocus()

        if (checkPlayServices()) {
            googleApiClient = GoogleApiClient.Builder(this)
                    .enableAutoManage(this)
                    {}
                    .addApi(Auth.CREDENTIALS_API)
                    .build()
        }

        setSignUpButtonState()

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
                        loginPresenter.login(email, password)
                    } else {
                        ProgressHelper.dismiss(progressBar)
                        response.errorBody()
                        val errorConverter = retrofit.responseBodyConverter<RegistrationResponse>(RegistrationResponse::class.java, arrayOfNulls<Annotation>(0))
                        var error: RegistrationResponse? = null
                        try {
                            error = errorConverter.convert(response.errorBody())
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

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_from_start, R.anim.slide_out_to_end)
    }

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

    private fun getErrorString(values: Array<String>?): String? {
        if (values == null || values.isEmpty()) return null
        val sb = StringBuilder()
        for (i in values.indices) {
            sb.append(values[i])
            if (i != values.size - 1) {
                sb.append(ERROR_DELIMITER)
            }
        }
        return sb.toString()
    }


    override fun onFailLogin(type: LoginFailType, credential: Credential?) {
        ProgressHelper.dismiss(progressBar)
        Toast.makeText(this, getMessageFor(type), Toast.LENGTH_SHORT).show()
    }

    override fun onSuccessLogin(authData: AuthData?) {
        ProgressHelper.dismiss(progressBar)
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
                        analytic.reportEvent(Analytic.SmartLock.SHOW_SAVE_REGISTRATION)
                        status.startResolutionForResult(this, RegisterActivity.RC_SAVE)
                    } else {
                        analytic.reportEventWithName(Analytic.SmartLock.DISABLED_REGISTRATION, status.statusMessage)
                        openMainFeed()
                    }
                }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RegisterActivity.RC_SAVE) {
            if (resultCode == RESULT_OK) {
                analytic.reportEvent(Analytic.SmartLock.REGISTRATION_SAVED)
            } else {
                analytic.reportEvent(Analytic.SmartLock.REGISTRATION_NOT_SAVED)
            }
            openMainFeed()
        }

    }

    private fun openMainFeed() {
        screenManager.showMainFeed(this, courseFromExtra)
    }

    override fun onLoadingWhileLogin() {
        hideSoftKeypad()
        ProgressHelper.activate(progressBar)
    }

    override fun onSocialLoginWithExistingEmail(email: String) {}

}
