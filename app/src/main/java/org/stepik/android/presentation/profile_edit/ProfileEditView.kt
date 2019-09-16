package org.stepik.android.presentation.profile_edit

import org.stepik.android.model.user.ProfileWrapper

interface ProfileEditView {
    sealed class State {
        object Idle : State()
        object Error : State()
        object Loading : State()
        class ProfileLoaded(val profileWrapper: ProfileWrapper) : State()
    }

    fun setState(state: State)
}