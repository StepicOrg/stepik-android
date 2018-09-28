package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_notification_settings.*
import org.stepic.droid.R
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.notifications.model.NotificationType

class NotificationSettingsFragment : FragmentBase() {
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
        fragmentSettingsNotificationLearnSwitch.setOnCheckedChangeListener(null)
        fragmentSettingsNotificationCommentSwitch.setOnCheckedChangeListener(null)
        fragmentSettingsNotificationTeachingSwitch.setOnCheckedChangeListener(null)
        fragmentSettingsNotificationOtherSwitch.setOnCheckedChangeListener(null)
        fragmentSettingsNotificationReviewSwitch.setOnCheckedChangeListener(null)
        fragmentSettingsNotificationVibrationSwitch.setOnCheckedChangeListener(null)
        fragmentSettingsNotificationSoundSwitch.setOnCheckedChangeListener(null)
    }


    private fun setUpNotificationVibration() {
        fragmentSettingsNotificationVibrationSwitch.isChecked = userPreferences.isVibrateNotificationEnabled
        fragmentSettingsNotificationVibrationSwitch.setOnCheckedChangeListener { _, isChecked -> userPreferences.isVibrateNotificationEnabled = isChecked }
    }

    private fun setUpSound() {
        fragmentSettingsNotificationSoundSwitch.isChecked = userPreferences.isSoundNotificationEnabled
        fragmentSettingsNotificationSoundSwitch.setOnCheckedChangeListener { _, isChecked -> userPreferences.setNotificationSoundEnabled(isChecked) }
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


}
