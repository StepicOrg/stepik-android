package org.stepik.android.presentation.profile_activities


interface ProfileActivitiesView {
    sealed class State {
        object Idle : State()
        object Loading : State()

        class Content : State()
    }

    fun setState(state: State)
    fun showNetworkError()
}