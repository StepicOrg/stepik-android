package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_settings.*
import org.stepic.droid.R
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.notifications.model.NotificationType
import org.stepic.droid.ui.dialogs.AllowMobileDataDialogFragment
import org.stepic.droid.ui.dialogs.VideoQualityDialog

class SettingsFragment : FragmentBase(), AllowMobileDataDialogFragment.Callback {

    companion object {
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }

    val kb: String by lazy { getString(R.string.kb) }

    val mb: String by lazy { getString(R.string.mb) }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpNotificationVibration()

        setUpNotifications()

        setUpSound()

        fragmentSettingsWifiEnableSwitch.isChecked = !sharedPreferenceHelper.isMobileInternetAlsoAllowed//if first time it is true

        fragmentSettingsExternalPlayerSwitch.isChecked = userPreferences.isOpenInExternal

        fragmentSettingsExternalPlayerSwitch.setOnCheckedChangeListener { _, isChecked -> userPreferences.isOpenInExternal = isChecked }

        fragmentSettingsCalendarWidgetSwitch.isChecked = userPreferences.isNeedToShowCalendarWidget

        fragmentSettingsCalendarWidgetSwitch.setOnCheckedChangeListener { _, isChecked -> userPreferences.isNeedToShowCalendarWidget = isChecked }

        fragmentSettingsKeepScreenOnSwitch.isChecked = userPreferences.isKeepScreenOnSteps
        fragmentSettingsKeepScreenOnSwitch.setOnCheckedChangeListener { _, isChecked -> userPreferences.isKeepScreenOnSteps = isChecked }

        fragmentSettingsDiscountingPolicySwitch.isChecked = userPreferences.isShowDiscountingPolicyWarning

        fragmentSettingsDiscountingPolicySwitch.setOnCheckedChangeListener { _, isChecked -> userPreferences.isShowDiscountingPolicyWarning = isChecked }


        fragmentSettingsWifiEnableSwitch.setOnCheckedChangeListener { _, newCheckedState ->
            if (fragmentSettingsWifiEnableSwitch.isUserTriggered) {
                if (newCheckedState) {
                    //wifi only
                    onMobileDataStateChanged(false)
                } else {
                    //wifi and mobile internet
                    fragmentSettingsWifiEnableSwitch.isChecked = true
                    val dialogFragment = AllowMobileDataDialogFragment.newInstance()
                    dialogFragment.setTargetFragment(this@SettingsFragment, 0)
                    if (!dialogFragment.isAdded) {
                        dialogFragment.show(fragmentManager, null)
                    }
                }

            }
        }


        videoQualityView.setOnClickListener {
            val videoDialog = VideoQualityDialog.newInstance(forPlaying = false)
            if (!videoDialog.isAdded) {
                videoDialog.show(fragmentManager, null)
            }
        }

        videoPlayingQualityView.setOnClickListener {
            val videoDialog = VideoQualityDialog.newInstance(forPlaying = true)
            if (!videoDialog.isAdded) {
                videoDialog.show(fragmentManager, null)
            }
        }

        storageManagementButton.setOnClickListener { screenManager.showStorageManagement(activity) }

    }

    private fun setUpNotificationVibration() {
        fragmentSettingsSotificationVibrationSwitch.isChecked = userPreferences.isVibrateNotificationEnabled
        fragmentSettingsSotificationVibrationSwitch.setOnCheckedChangeListener { _, isChecked -> userPreferences.isVibrateNotificationEnabled = isChecked }
    }

    private fun setUpSound() {
        fragmentSettingsNotificationSoundSwitch.isChecked = userPreferences.isSoundNotificationEnabled
        fragmentSettingsNotificationSoundSwitch.setOnCheckedChangeListener { _, isChecked -> userPreferences.setNotificationSoundEnabled(isChecked) }
    }

    override fun onDestroyView() {
        fragmentSettingsKeepScreenOnSwitch.setOnCheckedChangeListener(null)
        fragmentSettingsDiscountingPolicySwitch.setOnCheckedChangeListener(null)
        fragmentSettingsCalendarWidgetSwitch.setOnCheckedChangeListener(null)
        fragmentSettingsWifiEnableSwitch.setOnCheckedChangeListener(null)
        fragmentSettingsExternalPlayerSwitch.setOnCheckedChangeListener(null)
        fragmentSettingsNotificationLearnSwitch.setOnCheckedChangeListener(null)
        fragmentSettingsNotificationCommentSwitch.setOnCheckedChangeListener(null)
        fragmentSettingsNotificationTeachingSwitch.setOnCheckedChangeListener(null)
        fragmentSettingsNotificationOtherSwitch.setOnCheckedChangeListener(null)
        fragmentSettingsNotificationReviewSwitch.setOnCheckedChangeListener(null)
        fragmentSettingsSotificationVibrationSwitch.setOnCheckedChangeListener(null)
        fragmentSettingsNotificationSoundSwitch.setOnCheckedChangeListener(null)
        storageManagementButton.setOnClickListener(null)
        super.onDestroyView()
    }

    private fun storeMobileState(isMobileAllowed: Boolean) {
        sharedPreferenceHelper.setMobileInternetAndWifiAllowed(isMobileAllowed)
    }

    private fun setUpNotifications() {
        fragmentSettingsNotificationLearnSwitch.isChecked = userPreferences.isNotificationEnabled(NotificationType.learn)
        fragmentSettingsNotificationLearnSwitch.setOnCheckedChangeListener { _, isChecked -> userPreferences.setNotificationEnabled(NotificationType.learn, isChecked) }

        fragmentSettingsNotificationCommentSwitch.isChecked = userPreferences.isNotificationEnabled(NotificationType.comments)
        fragmentSettingsNotificationCommentSwitch.setOnCheckedChangeListener { _, isChecked -> userPreferences.setNotificationEnabled(NotificationType.comments, isChecked) }

        fragmentSettingsNotificationReviewSwitch.isChecked = userPreferences.isNotificationEnabled(NotificationType.review)
        fragmentSettingsNotificationReviewSwitch.setOnCheckedChangeListener { _, isChecked -> userPreferences.setNotificationEnabled(NotificationType.review, isChecked) }

        fragmentSettingsNotificationTeachingSwitch.isChecked = userPreferences.isNotificationEnabled(NotificationType.teach)
        fragmentSettingsNotificationTeachingSwitch.setOnCheckedChangeListener { _, isChecked -> userPreferences.setNotificationEnabled(NotificationType.teach, isChecked) }

        fragmentSettingsNotificationOtherSwitch.isChecked = userPreferences.isNotificationEnabled(NotificationType.other)
        fragmentSettingsNotificationOtherSwitch.setOnCheckedChangeListener { _, isChecked -> userPreferences.setNotificationEnabled(NotificationType.other, isChecked) }

    }

    override fun onMobileDataStateChanged(isMobileAllowed: Boolean) {
        fragmentSettingsWifiEnableSwitch.isChecked = !isMobileAllowed
        storeMobileState(isMobileAllowed)
    }
}