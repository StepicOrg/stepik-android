package org.stepik.android.presentation.achievement

import org.stepik.android.domain.achievement.model.AchievementItem

interface AchievementsView {
    sealed class State {
        object Idle : State()
        object SilentLoading : State()
        object Loading : State()
        class AchievementsLoaded(val achievements: List<AchievementItem>, val isMyProfile: Boolean) : State()
        object Error : State()
        object NoAchievements : State()
    }

    fun setState(state: State)
}