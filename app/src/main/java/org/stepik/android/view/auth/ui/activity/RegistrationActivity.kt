package org.stepik.android.view.auth.ui.activity

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.core.widget.CompoundButtonCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import dev.androidbroadcast.vbpd.viewBinding
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.analytic.LoginInteractionType
import org.stepic.droid.base.App
import org.stepic.droid.databinding.ActivityRegistrationBinding
import org.stepic.droid.ui.activities.SmartLockActivityBase
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment
import org.stepic.droid.ui.util.setOnKeyboardOpenListener
import org.stepic.droid.ui.util.snackbar
import org.stepic.droid.util.ProgressHelper
import org.stepic.droid.util.ValidatorUtil
import org.stepic.droid.util.resolveColorAttribute
import org.stepic.droid.util.stripUnderlinesFromLinks
import org.stepic.droid.util.toBundle
import org.stepik.android.domain.auth.mapper.RegistrationConsentMapper
import org.stepik.android.domain.auth.mapper.RegistrationConsentResult
import org.stepik.android.domain.auth.model.RegistrationConsentState
import org.stepik.android.domain.feature.interactor.FeaturesInteractor
import org.stepik.android.model.Course
import org.stepik.android.model.user.RegistrationCredentials
import org.stepik.android.presentation.auth.RegistrationPresenter
import org.stepik.android.presentation.auth.RegistrationView
import org.stepik.android.view.auth.model.AutoAuth
import org.stepik.android.view.base.ui.span.TypefaceSpanCompat
import ru.nobird.android.view.base.ui.extension.hideKeyboard
import javax.inject.Inject
import kotlin.getValue

class RegistrationActivity : SmartLockActivityBase(), RegistrationView {
    private val binding: ActivityRegistrationBinding by viewBinding(ActivityRegistrationBinding::bind)

    companion object {
        private const val ERROR_DELIMITER = " "

        private const val EXTRA_COURSE = "course"

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }

        fun createIntent(context: Context, course: Course? = null): Intent =
            Intent(context, RegistrationActivity::class.java)
                .putExtra(EXTRA_COURSE, course)
    }

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var featuresInteractor: FeaturesInteractor

    private val registrationPresenter: RegistrationPresenter by viewModels { viewModelFactory }

    private val passwordTooShortMessage by lazy {
        resources.getString(R.string.password_too_short)
    }
    private val consentRequiredMessage by lazy {
        resources.getString(R.string.registration_consent_required)
    }

    private val progressDialogFragment: DialogFragment =
        LoadingProgressDialogFragment.newInstance()
    private var consentState: RegistrationConsentState = RegistrationConsentState.fromFeatureSnapshot(false)
    private val requiredConsentDefaultTint by lazy {
        createRequiredConsentTint(
            checkedColor = resolveColorAttribute(R.attr.colorSecondary),
            uncheckedColor = resolveColorAttribute(R.attr.colorOnSurface)
        )
    }
    private val requiredConsentErrorTint by lazy {
        createRequiredConsentTint(
            checkedColor = resolveColorAttribute(R.attr.colorSecondary),
            uncheckedColor = resolveColorAttribute(R.attr.colorError)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        injectComponent()

        consentState = RegistrationConsentState.fromFeatureSnapshot(
            featuresInteractor.isAuthMarketingAgreementEnabledCached()
        )

        initTitle()

        binding.requiredConsentText.movementMethod = LinkMovementMethod.getInstance()
        binding.requiredConsentText.text = textResolver.fromHtml(consentRequiredMessage)
        stripUnderlinesFromLinks(binding.requiredConsentText)

        binding.requiredConsentCheckBox.setOnCheckedChangeListener { _, _ ->
            clearRequiredConsentError()
        }
        clearRequiredConsentError()

        if (consentState.isMarketingVisible) {
            binding.marketingConsentRow.isVisible = true
            binding.marketingHelperText.isVisible = true
            binding.marketingConsentCheckBox.isChecked = consentState.isMarketingChecked
        }

        binding.signUpButton.setOnClickListener { submit(LoginInteractionType.button) }

        binding.passwordField.setOnEditorActionListener { _, actionId, _ ->
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
                clearRequiredConsentError()
            }
        }

        binding.firstNameField.addTextChangedListener(reportAnalyticWhenTextBecomeNotBlank)
        binding.emailField.addTextChangedListener(reportAnalyticWhenTextBecomeNotBlank)
        binding.passwordField.addTextChangedListener(reportAnalyticWhenTextBecomeNotBlank)

        val onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                analytic.reportEvent(Analytic.Registration.TAP_ON_FIELDS)
            }
        }
        binding.firstNameField.onFocusChangeListener = onFocusChangeListener
        binding.emailField.onFocusChangeListener = onFocusChangeListener
        binding.passwordField.onFocusChangeListener = onFocusChangeListener

        binding.firstNameField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                binding.emailField.requestFocus()
                true
            } else {
                false
            }
        }

        binding.emailField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                binding.passwordField.requestFocus()
                true
            } else {
                false
            }
        }

        binding.registerRootView.requestFocus()

        initGoogleApiClient()

        setSignUpButtonState()

        setOnKeyboardOpenListener(binding.rootView, {
            binding.stepikLogo.isVisible = false
            binding.signUpText.isVisible = false
        }, {
            binding.stepikLogo.isVisible = true
            binding.signUpText.isVisible = true
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
        binding.signUpButton.isEnabled =
            binding.emailField.text.isNullOrBlank() == false &&
            binding.firstNameField.text.isNullOrBlank() == false &&
            binding.passwordField.text.isNullOrBlank() == false
    }

    private fun initTitle() {
        val signUpString = getString(R.string.sign_up)
        val signUpSuffix = getString(R.string.sign_up_with_email_suffix)

        val spannableSignIn = SpannableString(signUpString + signUpSuffix)
        val typeface = ResourcesCompat.getFont(this, R.font.roboto_medium)

        spannableSignIn.setSpan(TypefaceSpanCompat(typeface), 0, signUpString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.signUpText.text = spannableSignIn
    }

    private fun submit(interactionType: LoginInteractionType) {
        analytic.reportEvent(Analytic.Registration.CLICK_WITH_INTERACTION_TYPE, interactionType.toBundle())
        currentFocus?.hideKeyboard()

        val firstName = binding.firstNameField.text.toString().trim()
        val lastName = " " // registrationSecondName.text.toString().trim()
        val email = binding.emailField.text.toString().trim()
        val password = binding.passwordField.text.toString()

        analytic.reportEvent(Analytic.Interaction.CLICK_REGISTER_BUTTON)

        var isOk = true

        if (!ValidatorUtil.isPasswordValid(password)) {
            showError(passwordTooShortMessage)
            isOk = false
        }

        val currentState = getCurrentConsentState()

        val consentResult = RegistrationConsentMapper.validate(currentState)
        if (consentResult is RegistrationConsentResult.RequiredConsentMissing) {
            showRequiredConsentError(showErrorText = isOk)
            isOk = false
        }

        if (isOk) {
            val subscribedForMarketing =
                RegistrationConsentMapper.mapSubscribedForMarketing(currentState)
            registrationPresenter.submit(
                RegistrationCredentials(
                    firstName,
                    lastName,
                    email,
                    password,
                    subscribedForMarketing
                )
            )
        }
    }

    private fun getCurrentConsentState(): RegistrationConsentState =
        RegistrationConsentState(
            isRequiredConsentGranted = binding.requiredConsentCheckBox.isChecked,
            isMarketingVisible = binding.marketingConsentRow.isVisible,
            isMarketingChecked = binding.marketingConsentCheckBox.isChecked
        )

    private fun showRequiredConsentError(showErrorText: Boolean = false) {
        binding.consentErrorText.isVisible = showErrorText
        CompoundButtonCompat.setButtonTintList(
            binding.requiredConsentCheckBox,
            requiredConsentErrorTint
        )
    }

    private fun clearRequiredConsentError() {
        binding.consentErrorText.isVisible = false
        CompoundButtonCompat.setButtonTintList(
            binding.requiredConsentCheckBox,
            requiredConsentDefaultTint
        )
    }

    private fun createRequiredConsentTint(checkedColor: Int, uncheckedColor: Int): ColorStateList =
        ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ),
            intArrayOf(
                checkedColor,
                uncheckedColor
            )
        )

    override fun setState(state: RegistrationView.State) {
        if (state is RegistrationView.State.Loading) {
            ProgressHelper.activate(progressDialogFragment, supportFragmentManager, LoadingProgressDialogFragment.TAG)
        } else {
            ProgressHelper.dismiss(supportFragmentManager, LoadingProgressDialogFragment.TAG)
        }

        when (state) {
            is RegistrationView.State.Idle -> {
                binding.signUpButton.isEnabled = true
                binding.registerForm.isEnabled = true
                binding.registerErrorMessage.isVisible = false
            }

            is RegistrationView.State.Error -> {
                showError(getErrorString(state.data.email))
                showError(getErrorString(state.data.firstName))
                showError(getErrorString(state.data.lastName))
                showError(getErrorString(state.data.password))
            }

            is RegistrationView.State.Success -> {
                screenManager
                    .showLogin(
                        this,
                        state.credentials.login,
                        state.credentials.password,
                        AutoAuth.REGISTRATION,
                        intent.getParcelableExtra(EXTRA_COURSE)
                    )
            }

            else -> Unit
        }
    }

    override fun showNetworkError() {
        binding.registerRootView.snackbar(messageRes = R.string.connectionProblems)
    }

    override fun applyTransitionPrev() {} // we need default system animation

    private fun showError(errorText: String?) {
        errorText?.let {
            analytic.reportEventWithName(Analytic.Registration.ERROR, errorText)
            if (binding.registerErrorMessage.visibility == View.GONE) {
                binding.signUpButton.isEnabled = false
                binding.registerForm.isEnabled = false
                binding.registerErrorMessage.text = it
                binding.registerErrorMessage.isVisible = true
            }
        }
    }

    private fun getErrorString(values: List<String?>?): String? =
        values
            ?.takeIf { it.isNotEmpty() }
            ?.joinToString(separator = ERROR_DELIMITER)
}
