package org.stepik.android.presentation.achievement

import org.stepic.droid.model.AchievementFlatItem

interface AchievementsView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        class AchievementsLoaded(val achievements: List<AchievementFlatItem>) : State()
        object Error : State()
    }

    fun setState(state: State)
}