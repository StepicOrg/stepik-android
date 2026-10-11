package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dev.androidbroadcast.vbpd.viewBinding
import org.stepic.droid.R
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.databinding.FragmentNotificationSettingsBinding
import org.stepic.droid.notifications.model.NotificationType

class NotificationSettingsFragment : FragmentBase() {
    private val notificationSettingsBinding: FragmentNotificationSettingsBinding by viewBinding(FragmentNotificationSettingsBinding::bind)

    companion object {
        fun newInstance(): NotificationSettingsFragment = NotificationSettingsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater.inflate(R.layout.fragment_notification_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nullifyActivityBackground()
        setUpNotificationVibration()
        setUpNotifications()
        setUpSound()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        notificationSettingsBinding.fragmentSettingsNotificationLearnSwitch.setOnCheckedChangeListener(null)
        notificationSettingsBinding.fragmentSettingsNotificationCommentSwitch.setOnCheckedChangeListener(null)
        notificationSettingsBinding.fragmentSettingsNotificationTeachingSwitch.setOnCheckedChangeListener(null)
        notificationSettingsBinding.fragmentSettingsNotificationOtherSwitch.setOnCheckedChangeListener(null)
        notificationSettingsBinding.fragmentSettingsNotificationReviewSwitch.setOnCheckedChangeListener(null)
        notificationSettingsBinding.fragmentSettingsNotificationVibrationSwitch.setOnCheckedChangeListener(null)
        notificationSettingsBinding.fragmentSettingsNotificationSoundSwitch.setOnCheckedChangeListener(null)
    }


    private fun setUpNotificationVibration() {
        notificationSettingsBinding.fragmentSettingsNotificationVibrationSwitch.isChecked = userPreferences.isVibrateNotificationEnabled
        notificationSettingsBinding.fragmentSettingsNotificationVibrationSwitch.setOnCheckedChangeListener { _, isChecked -> userPreferences.isVibrateNotificationEnabled = isChecked }
    }

    private fun setUpSound() {
        notificationSettingsBinding.fragmentSettingsNotificationSoundSwitch.isChecked = userPreferences.isSoundNotificationEnabled
        notificationSettingsBinding.fragmentSettingsNotificationSoundSwitch.setOnCheckedChangeListener { _, isChecked -> userPreferences.setNotificationSoundEnabled(isChecked) }
    }

    private fun setUpNotifications() {
        notificationSettingsBinding.fragmentSettingsNotificationLearnSwitch.isChecked = userPreferences.isNotificationEnabled(NotificationType.learn)
        notificationSettingsBinding.fragmentSettingsNotificationLearnSwitch.setOnCheckedChangeListener { _, isChecked -> userPreferences.setNotificationEnabled(NotificationType.learn, isChecked) }

        notificationSettingsBinding.fragmentSettingsNotificationCommentSwitch.isChecked = userPreferences.isNotificationEnabled(NotificationType.comments)
        notificationSettingsBinding.fragmentSettingsNotificationCommentSwitch.setOnCheckedChangeListener { _, isChecked -> userPreferences.setNotificationEnabled(NotificationType.comments, isChecked) }

        notificationSettingsBinding.fragmentSettingsNotificationReviewSwitch.isChecked = userPreferences.isNotificationEnabled(NotificationType.review)
        notificationSettingsBinding.fragmentSettingsNotificationReviewSwitch.setOnCheckedChangeListener { _, isChecked -> userPreferences.setNotificationEnabled(NotificationType.review, isChecked) }

        notificationSettingsBinding.fragmentSettingsNotificationTeachingSwitch.isChecked = userPreferences.isNotificationEnabled(NotificationType.teach)
        notificationSettingsBinding.fragmentSettingsNotificationTeachingSwitch.setOnCheckedChangeListener { _, isChecked -> userPreferences.setNotificationEnabled(NotificationType.teach, isChecked) }

        notificationSettingsBinding.fragmentSettingsNotificationOtherSwitch.isChecked = userPreferences.isNotificationEnabled(NotificationType.other)
        notificationSettingsBinding.fragmentSettingsNotificationOtherSwitch.setOnCheckedChangeListener { _, isChecked -> userPreferences.setNotificationEnabled(NotificationType.other, isChecked) }

    }


}
