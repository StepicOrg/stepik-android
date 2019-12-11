package org.stepik.android.presentation.profile_courses

import org.stepik.android.domain.profile_activities.model.ProfileActivitiesData

interface ProfileCoursesView {
    sealed class State {
        object Idle : State()
        object SilentLoading : State()
        object Loading : State()
        object Empty : State()
        object Error : State()

        class Content(val profileActivitiesData: ProfileActivitiesData) : State()
    }

    fun setState(state: State)
}