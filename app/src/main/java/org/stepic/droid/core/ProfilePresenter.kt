package org.stepic.droid.core

import org.stepic.droid.core.presenters.PresenterBase
import org.stepic.droid.core.presenters.contracts.ProfileView

abstract class ProfilePresenter : PresenterBase<ProfileView>() {

    abstract fun initProfile()

    abstract fun initProfile(profileId: Long)

    abstract fun showStreakForStoredUser()
}
