package org.stepik.android.view.settings.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import dev.androidbroadcast.vbpd.viewBinding
import com.vk.api.sdk.VK
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.configuration.Config
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.databinding.FragmentSettingsBinding
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.preferences.UserPreferences
import org.stepic.droid.ui.dialogs.AllowMobileDataDialogFragment
import org.stepik.android.view.filter.ui.dialog.CoursesLangDialogFragment
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment
import org.stepic.droid.ui.dialogs.LogoutAreYouSureDialog
import org.stepic.droid.ui.dialogs.VideoQualityDialog
import org.stepic.droid.util.ProgressHelper
import org.stepik.android.domain.feedback.model.SupportEmailData
import org.stepik.android.presentation.settings.SettingsPresenter
import org.stepik.android.presentation.settings.SettingsView
import org.stepik.android.view.font_size_settings.ui.dialog.ChooseFontSizeDialogFragment
import org.stepik.android.view.in_app_web_view.ui.dialog.InAppWebViewDialogFragment
import org.stepik.android.view.settings.routing.DeleteAccountDeepLinkBuilder
import org.stepik.android.view.settings.ui.dialog.NightModeSettingDialogFragment
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import javax.inject.Inject

class SettingsFragment :
    Fragment(R.layout.fragment_settings),
    AllowMobileDataDialogFragment.Callback,
    LogoutAreYouSureDialog.Companion.OnLogoutSuccessListener,
    SettingsView,
    InAppWebViewDialogFragment.Callback {
    companion object {
        fun newInstance(): SettingsFragment =
            SettingsFragment()
    }

    private val binding: FragmentSettingsBinding by viewBinding(FragmentSettingsBinding::bind)

    private val presenter: SettingsPresenter by viewModels { viewModelFactory }

    private val progressDialogFragment: DialogFragment =
        LoadingProgressDialogFragment.newInstance()

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var config: Config

    @Inject
    internal lateinit var userPreferences: UserPreferences

    @Inject
    internal lateinit var deleteAccountDeepLinkBuilder: DeleteAccountDeepLinkBuilder

    @Inject
    internal lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.notificationActionButton.setOnClickListener {
            screenManager.showNotificationSettings(activity)
        }

        binding.fragmentSettingsWifiEnableSwitch.isChecked = !sharedPreferenceHelper.isMobileInternetAlsoAllowed // if first time it is true

        binding.fragmentSettingsExternalPlayerSwitch.isChecked = userPreferences.isOpenInExternal

        binding.fragmentSettingsExternalPlayerSwitch.setOnCheckedChangeListener { _, isChecked -> userPreferences.isOpenInExternal = isChecked }

        binding.fragmentSettingsCalendarWidgetSwitch.isChecked = userPreferences.isNeedToShowCalendarWidget

        binding.fragmentSettingsCalendarWidgetSwitch.setOnCheckedChangeListener { _, isChecked -> userPreferences.isNeedToShowCalendarWidget = isChecked }

        binding.fragmentSettingsKeepScreenOnSwitch.isChecked = userPreferences.isKeepScreenOnSteps
        binding.fragmentSettingsKeepScreenOnSwitch.setOnCheckedChangeListener { _, isChecked -> userPreferences.isKeepScreenOnSteps = isChecked }

        binding.fragmentSettingsAdaptiveMode.isChecked = userPreferences.isAdaptiveModeEnabled
        binding.fragmentSettingsAdaptiveMode.setOnCheckedChangeListener { _, isChecked -> userPreferences.isAdaptiveModeEnabled = isChecked }

        binding.fragmentSettingsDiscountingPolicySwitch.isChecked = userPreferences.isShowDiscountingPolicyWarning

        binding.fragmentSettingsDiscountingPolicySwitch.setOnCheckedChangeListener { _, isChecked -> userPreferences.isShowDiscountingPolicyWarning = isChecked }

        binding.fragmentSettingsAutoplay.isChecked = userPreferences.isAutoplayEnabled
        binding.fragmentSettingsAutoplay.setOnCheckedChangeListener { _, isChecked -> userPreferences.isAutoplayEnabled = isChecked }

        binding.fragmentSettingsWifiEnableSwitch.setOnCheckedChangeListener { _, newCheckedState ->
            if (binding.fragmentSettingsWifiEnableSwitch.isUserTriggered) {
                if (newCheckedState) {
                    // wifi only
                    onMobileDataStateChanged(false)
                } else {
                    // wifi and mobile internet
                    binding.fragmentSettingsWifiEnableSwitch.isChecked = true
                    val dialogFragment = AllowMobileDataDialogFragment.newInstance()
                    dialogFragment.setTargetFragment(this@SettingsFragment, 0)
                    dialogFragment.showIfNotExists(parentFragmentManager, AllowMobileDataDialogFragment.TAG)
                }
            }
        }

        binding.videoQualityView.setOnClickListener {
            VideoQualityDialog
                .newInstance(forPlaying = false)
                .showIfNotExists(childFragmentManager, VideoQualityDialog.TAG)
        }

        binding.videoPlayingQualityView.setOnClickListener {
            VideoQualityDialog
                .newInstance(forPlaying = true)
                .showIfNotExists(childFragmentManager, VideoQualityDialog.TAG)
        }

        binding.storageManagementButton.setOnClickListener { screenManager.showStorageManagement(activity) }

        binding.langWidgetActionButton.setOnClickListener {
            CoursesLangDialogFragment
                .newInstance()
                .showIfNotExists(childFragmentManager, CoursesLangDialogFragment.TAG)
        }

        binding.nightModeSettingsButton.setOnClickListener {
            NightModeSettingDialogFragment
                .newInstance()
                .showIfNotExists(childFragmentManager, NightModeSettingDialogFragment.TAG)
        }

        binding.fontSizeSettingsButton.setOnClickListener {
            ChooseFontSizeDialogFragment
                .newInstance()
                .showIfNotExists(childFragmentManager, ChooseFontSizeDialogFragment.TAG)
        }

        binding.downloadsSettingsButton.setOnClickListener {
            analytic.reportEvent(Analytic.Screens.USER_OPEN_DOWNLOADS)
            screenManager.showDownloads(requireContext())
        }

        binding.contactSupportButton.setOnClickListener {
            InAppWebViewDialogFragment
                .newInstance(getString(R.string.settings_contact_support), config.supportUrl)
                .showIfNotExists(childFragmentManager, InAppWebViewDialogFragment.TAG)
        }

        binding.helpCenterButton.setOnClickListener {
            InAppWebViewDialogFragment
                .newInstance(getString(R.string.settings_help_center), getString(R.string.settings_help_center_url))
                .showIfNotExists(childFragmentManager, InAppWebViewDialogFragment.TAG)
        }

        binding.feedbackSettingsButton.setOnClickListener {
            analytic.reportEvent(Analytic.Screens.USER_OPEN_FEEDBACK)
            screenManager.openFeedbackActivity(requireActivity())
        }

        binding.aboutSettingsButton.setOnClickListener {
            analytic.reportEvent(Analytic.Screens.USER_OPEN_ABOUT_APP)
            screenManager.openAboutActivity(requireActivity())
        }

        binding.deleteAccountButton.setOnClickListener {
            analytic.reportAmplitudeEvent(AmplitudeAnalytic.Settings.DELETE_ACCOUNT_CLICKED)
            InAppWebViewDialogFragment
                .newInstance(
                    title = getString(R.string.settings_delete_account),
                    url = deleteAccountDeepLinkBuilder.buildDeleteAccountUrl(),
                    isProvideAuth = true
                )
                .showIfNotExists(childFragmentManager, InAppWebViewDialogFragment.TAG)
        }

        binding.logoutSettingsButton.setOnClickListener {
            val supportFragmentManager = activity
                ?.supportFragmentManager
                ?: return@setOnClickListener

            val dialog = LogoutAreYouSureDialog.newInstance()
            dialog.setTargetFragment(this, 0)
            dialog.showIfNotExists(supportFragmentManager, LogoutAreYouSureDialog.TAG)
            analytic.reportEvent(Analytic.Screens.USER_LOGOUT)
        }
    }

    private fun injectComponent() {
        App.component()
            .settingsComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onStart() {
        super.onStart()
        presenter.attachView(this)
    }

    override fun onStop() {
        presenter.detachView(this)
        super.onStop()
    }

    override fun onDestroyView() {
        binding.fragmentSettingsKeepScreenOnSwitch.setOnCheckedChangeListener(null)
        binding.fragmentSettingsDiscountingPolicySwitch.setOnCheckedChangeListener(null)
        binding.fragmentSettingsCalendarWidgetSwitch.setOnCheckedChangeListener(null)
        binding.fragmentSettingsWifiEnableSwitch.setOnCheckedChangeListener(null)
        binding.fragmentSettingsExternalPlayerSwitch.setOnCheckedChangeListener(null)
        binding.storageManagementButton.setOnClickListener(null)
        binding.notificationActionButton.setOnClickListener(null)
        super.onDestroyView()
    }

    private fun storeMobileState(isMobileAllowed: Boolean) {
        sharedPreferenceHelper.setMobileInternetAndWifiAllowed(isMobileAllowed)
    }

    override fun onMobileDataStateChanged(isMobileAllowed: Boolean) {
        binding.fragmentSettingsWifiEnableSwitch.isChecked = !isMobileAllowed
        storeMobileState(isMobileAllowed)
    }

    override fun onLogout() {
        presenter.onLogoutClicked()
    }

    override fun setBlockingLoading(isLoading: Boolean) {
        if (isLoading) {
            ProgressHelper.activate(progressDialogFragment, activity?.supportFragmentManager, LoadingProgressDialogFragment.TAG)
        } else {
            ProgressHelper.dismiss(activity?.supportFragmentManager, LoadingProgressDialogFragment.TAG)
        }
    }

    override fun sendTextFeedback(supportEmailData: SupportEmailData) {
        screenManager.openTextFeedBack(requireContext(), supportEmailData)
    }

    override fun onLogoutSuccess() {
//        LoginManager.getInstance().logOut()
        VK.logout()
        (activity as? SignOutListener)?.onSignOut()
        screenManager.showLaunchScreenAfterLogout(requireContext())
    }

    /**
     * InAppWebViewDialogFragment.Callback to check account deletion
     */
    override fun onDismissed() {
        presenter.handleAccountDeletion()
    }

    /***
     *  This callback is necessary, in order to sign out through
     *  Google APIClient in host activity
     */
    interface SignOutListener {
        fun onSignOut()
    }
}
