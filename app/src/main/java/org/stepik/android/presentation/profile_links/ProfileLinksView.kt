package org.stepik.android.presentation.profile_links

import org.stepik.android.model.SocialProfile

interface ProfileLinksView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        class ProfileLinksLoaded(val profileLinks: List<SocialProfile>) : State()
        object Error : State()
        object Empty : State()
    }

    fun setState(state: State)
}