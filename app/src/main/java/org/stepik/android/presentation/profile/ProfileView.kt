package org.stepik.android.presentation.profile

import org.stepic.droid.model.AchievementFlatItem

interface ProfileView {
    sealed class State {
        object Idle : State()
        object NetworkError : State()
        object UserNotFoundError: State()
        object NeedAuthError : State()
        object Loading : State()
        class ProfileLoaded() : State()
    }

    fun setState(state: State)

    // Achievements realated
    fun showAchievements(achievements: List<AchievementFlatItem>)
    fun onAchievementsLoadingError()
    fun onAchievementsLoading()

    // Streak related
    fun showNotificationEnabledState(notificationEnabled: Boolean, notificationTimeValue: String)
    fun hideNotificationTime(needHide: Boolean)
    fun setNewTimeInterval(timePresentationString: String)
}