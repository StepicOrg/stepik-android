package org.stepik.android.presentation.profile_notification

import org.stepik.android.domain.profile.model.ProfileData

interface ProfileNotificationView {
    fun setState(profileData: ProfileData?)
    fun showNotificationEnabledState(notificationEnabled: Boolean, notificationTimeValue: String)
    fun hideNotificationTime(needHide: Boolean)
    fun setNewTimeInterval(timePresentationString: String)
}