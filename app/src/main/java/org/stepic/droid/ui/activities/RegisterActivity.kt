package org.stepic.droid.ui.activities

import android.app.ProgressDialog
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.text.Editable
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.panel_custom_action_bar.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.core.LoginFailType
import org.stepic.droid.core.presenters.LoginPresenter
import org.stepic.droid.core.presenters.contracts.LoginView
import org.stepic.droid.util.ProgressHelper
import org.stepic.droid.util.ValidatorUtil
import org.stepic.droid.util.getMessageFor
import org.stepic.droid.web.RegistrationResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject


class RegisterActivity : FragmentActivityBase(), LoginView {
    companion object {
        val ERROR_DELIMITER = " "
        val TAG = "RegisterActivity"
    }

    private val passwordTooShortMessage by lazy {
        resources.getString(R.string.password_too_short)
    }
    private val termsMessageHtml by lazy {
        resources.getString(R.string.terms_message_register)
    }

    private lateinit var progressBar: ProgressDialog
    private lateinit var passwordWatcher: TextWatcher

    @Inject
    lateinit var loginPresenter: LoginPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(null)
        setContentView(org.stepic.droid.R.layout.activity_register)
        overridePendingTransition(org.stepic.droid.R.anim.slide_in_from_bottom, org.stepic.droid.R.anim.no_transition)
        hideSoftKeypad()
        App.getComponentManager().loginComponent(TAG).inject(this)

        termsPrivacyRegisterTextView.movementMethod = LinkMovementMethod.getInstance()
        termsPrivacyRegisterTextView.text = textResolver.fromHtml(termsMessageHtml)

        signUpButton.setOnClickListener { createAccount() }
        actionbarCloseButtonLayout.setOnClickListener { finish() }

        progressBar = ProgressDialog(this)
        progressBar.setTitle(getString(R.string.loading))
        progressBar.setMessage(getString(R.string.loading_message))
        progressBar.setCancelable(false)

        passwordTextView.setOnEditorActionListener { _, actionId, _ ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                analytic.reportEvent(Analytic.Login.CLICK_REGISTRATION_SEND_IME)
                createAccount()
                handled = true
            }
            handled
        }

        passwordWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                if (ValidatorUtil.isPasswordLengthValid(s.length)) {
                    hideError(passwordWrapper)
                }
            }
        }
        passwordTextView.addTextChangedListener(passwordWatcher)

        val onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus -> setClearErrorOnFocus(v, hasFocus) }
        emailView.onFocusChangeListener = onFocusChangeListener
        registrationFirstName.onFocusChangeListener = onFocusChangeListener
        registrationSecondName.onFocusChangeListener = onFocusChangeListener

        registerRootView.requestFocus()

        loginPresenter.attachView(this)
    }


    private fun createAccount() {
        val firstName = registrationFirstName.text.toString().trim()
        val lastName = registrationSecondName.text.toString().trim()
        val email = emailView.text.toString().trim()
        val password = passwordTextView.text.toString()

        analytic.reportEvent(Analytic.Interaction.CLICK_REGISTER_BUTTON)

        var isOk = true

        if (!ValidatorUtil.isPasswordValid(password)) {
            showError(passwordWrapper, passwordTooShortMessage)
            isOk = false
        }

        if (isOk) {
            hideError(registrationFirstNameWrapper)
            hideError(registrationSecondNameWrapper)
            hideError(emailViewWrapper)
            hideError(passwordWrapper)
            onLoadingWhileLogin()
            shell.api.signUp(firstName, lastName, email, password).enqueue(object : Callback<RegistrationResponse> {
                override fun onResponse(call: Call<RegistrationResponse>, response: Response<RegistrationResponse>) {
                    ProgressHelper.dismiss(progressBar)
                    if (response.isSuccessful) {
                        analytic.reportEvent(FirebaseAnalytics.Event.SIGN_UP)
                        loginPresenter.login(email, password)
                    } else {
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

    override fun onDestroy() {
        loginPresenter.detachView(this)
        passwordTextView.removeTextChangedListener(passwordWatcher)
        passwordTextView.setOnEditorActionListener(null)
        if (isFinishing) {
            App.getComponentManager().releaseLoginComponent(TAG)
        }
        super.onDestroy()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(org.stepic.droid.R.anim.no_transition, org.stepic.droid.R.anim.slide_out_to_bottom)
    }

    private fun hideError(textInputLayout: TextInputLayout?) {
        if (textInputLayout != null) {
            textInputLayout.error = ""
            textInputLayout.isErrorEnabled = false
        }
    }

    private fun showError(textInputLayout: TextInputLayout?, errorText: String?) {
        if (textInputLayout != null && errorText != null) {
            textInputLayout.isErrorEnabled = true
            textInputLayout.error = errorText
        }
    }

    private fun handleErrorRegistrationResponse(registrationResponse: RegistrationResponse?) {
        if (registrationResponse == null) return
        showError(emailViewWrapper, getErrorString(registrationResponse.email))
        showError(registrationFirstNameWrapper, getErrorString(registrationResponse.first_name))
        showError(registrationSecondNameWrapper, getErrorString(registrationResponse.last_name))
        showError(passwordWrapper, getErrorString(registrationResponse.password))
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

    fun setClearErrorOnFocus(view: View?, hasFocus: Boolean) {
        if (hasFocus) {
            if (view?.id == R.id.emailView) {
                hideError(emailViewWrapper)
            }
            if (view?.id == R.id.registrationFirstName) {
                hideError(registrationFirstNameWrapper)
            }
            if (view?.id == R.id.registrationSecondName) {
                hideError(registrationSecondNameWrapper)
            }
        }
    }


    override fun onFailLogin(type: LoginFailType) {
        ProgressHelper.dismiss(progressBar)
        Toast.makeText(this, getMessageFor(type), Toast.LENGTH_SHORT).show()
    }

    override fun onSuccessLogin() {
        ProgressHelper.dismiss(progressBar)
        shell.screenProvider.showMainFeed(this, courseFromExtra)
    }

    override fun onLoadingWhileLogin() {
        hideSoftKeypad()
        ProgressHelper.activate(progressBar)
    }

}
