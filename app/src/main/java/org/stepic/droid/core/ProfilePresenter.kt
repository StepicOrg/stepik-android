package org.stepic.droid.core

import org.stepic.droid.core.presenters.PresenterWithPotentialLeak
import org.stepic.droid.core.presenters.contracts.ProfileView

abstract class ProfilePresenter : PresenterWithPotentialLeak<ProfileView>() {

    abstract fun initProfile()

    abstract fun initProfile(profileId: Long)

    abstract fun showStreakForStoredUser()
}
