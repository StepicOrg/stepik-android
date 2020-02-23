package org.stepik.android.view.auth.ui.activity

import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_register.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.analytic.LoginInteractionType
import org.stepic.droid.base.App
import org.stepic.droid.ui.activities.SmartLockActivityBase
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment
import org.stepic.droid.ui.util.setOnKeyboardOpenListener
import org.stepic.droid.ui.util.snackbar
import org.stepic.droid.util.ProgressHelper
import org.stepic.droid.util.ValidatorUtil
import org.stepic.droid.util.stripUnderlinesFromLinks
import org.stepic.droid.util.toBundle
import org.stepik.android.model.user.RegistrationCredentials
import org.stepik.android.presentation.auth.RegistrationPresenter
import org.stepik.android.presentation.auth.RegistrationView
import org.stepik.android.view.base.ui.span.TypefaceSpanCompat
import ru.nobird.android.view.base.ui.extension.hideKeyboard
import javax.inject.Inject

class RegisterActivity : SmartLockActivityBase(), RegistrationView {
    companion object {
        const val ERROR_DELIMITER = " "
        const val TAG = "RegisterActivity"

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var registrationPresenter: RegistrationPresenter

    private val passwordTooShortMessage by lazy {
        resources.getString(R.string.password_too_short)
    }
    private val termsMessageHtml by lazy {
        resources.getString(R.string.terms_message_register)
    }

    private val progressDialogFragment: DialogFragment =
        LoadingProgressDialogFragment.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        injectComponent()
        registrationPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(RegistrationPresenter::class.java)

        initTitle()

        termsPrivacyRegisterTextView.movementMethod = LinkMovementMethod.getInstance()
        termsPrivacyRegisterTextView.text = textResolver.fromHtml(termsMessageHtml)
        stripUnderlinesFromLinks(termsPrivacyRegisterTextView)

        signUpButton.setOnClickListener { submit(LoginInteractionType.button) }

        passwordField.setOnEditorActionListener { _, actionId, _ ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                analytic.reportEvent(Analytic.Registration.CLICK_SEND_IME)
                submit(LoginInteractionType.ime)
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
                registrationPresenter.onFormChanged()
                setSignUpButtonState()
            }
        }

        firstNameField.addTextChangedListener(reportAnalyticWhenTextBecomeNotBlank)
        emailField.addTextChangedListener(reportAnalyticWhenTextBecomeNotBlank)
        passwordField.addTextChangedListener(reportAnalyticWhenTextBecomeNotBlank)

        val onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                analytic.reportEvent(Analytic.Registration.TAP_ON_FIELDS)
            }
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
            stepikLogo.isVisible = false
            signUpText.isVisible = false
        }, {
            stepikLogo.isVisible = true
            signUpText.isVisible = true
        })

    }

    private fun injectComponent() {
        App.component()
            .authComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onStart() {
        super.onStart()
        registrationPresenter.attachView(this)
    }

    override fun onStop() {
        registrationPresenter.detachView(this)
        super.onStop()
    }

    private fun setSignUpButtonState() {
        signUpButton.isEnabled = emailField.text.isNotBlank() && firstNameField.text.isNotBlank() && passwordField.text.isNotBlank()
    }

    private fun initTitle() {
        val signUpString = getString(R.string.sign_up)
        val signUpSuffix = getString(R.string.sign_up_with_email_suffix)

        val spannableSignIn = SpannableString(signUpString + signUpSuffix)
        val typeface = ResourcesCompat.getFont(this, R.font.roboto_medium)

        spannableSignIn.setSpan(TypefaceSpanCompat(typeface), 0, signUpString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        signUpText.text = spannableSignIn
    }

    private fun submit(interactionType: LoginInteractionType) {
        analytic.reportEvent(Analytic.Registration.CLICK_WITH_INTERACTION_TYPE, interactionType.toBundle())
        currentFocus?.hideKeyboard()

        val firstName = firstNameField.text.toString().trim()
        val lastName = " " // registrationSecondName.text.toString().trim()
        val email = emailField.text.toString().trim()
        val password = passwordField.text.toString()

        analytic.reportEvent(Analytic.Interaction.CLICK_REGISTER_BUTTON)

        var isOk = true

        if (!ValidatorUtil.isPasswordValid(password)) {
            showError(passwordTooShortMessage) // todo
            isOk = false
        }

        if (isOk) {
            registrationPresenter.submit(RegistrationCredentials(firstName, lastName, email, password))
        }
    }

    override fun setState(state: RegistrationView.State) {
        if (state is RegistrationView.State.Loading) {
            ProgressHelper.activate(progressDialogFragment, supportFragmentManager, LoadingProgressDialogFragment.TAG)
        } else {
            ProgressHelper.dismiss(supportFragmentManager, LoadingProgressDialogFragment.TAG)
        }

        when (state) {
            is RegistrationView.State.Idle -> {
                signUpButton.isEnabled = true
                registerForm.isEnabled = true
                registerErrorMessage.isVisible = false
            }

            is RegistrationView.State.Error -> {
                showError(getErrorString(state.data.email))
                showError(getErrorString(state.data.firstName))
                showError(getErrorString(state.data.lastName))
                showError(getErrorString(state.data.password))
            }

            is RegistrationView.State.Success -> {
                screenManager.showLogin(this, state.credentials.login, state.credentials.password, true, courseFromExtra)
            }
        }
    }

    override fun showNetworkError() {
        registerRootView.snackbar(messageRes = R.string.connectionProblems)
    }

    override fun applyTransitionPrev() {} // we need default system animation

    private fun showError(errorText: String?) {
        errorText?.let {
            analytic.reportEventWithName(Analytic.Registration.ERROR, errorText)
            if (registerErrorMessage.visibility == View.GONE) {
                signUpButton.isEnabled = false
                registerForm.isEnabled = false
                registerErrorMessage.text = it
                registerErrorMessage.isVisible = true
            }
        }
    }

    private fun getErrorString(values: List<String?>?): String? =
        values
            ?.takeIf { it.isNotEmpty() }
            ?.joinToString(separator = ERROR_DELIMITER)
}