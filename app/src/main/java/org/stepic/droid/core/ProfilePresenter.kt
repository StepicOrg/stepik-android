package org.stepic.droid.core

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.core.presenters.PresenterWithPotentialLeak
import org.stepic.droid.core.presenters.contracts.ProfileView

abstract class ProfilePresenter (analytic: Analytic) : PresenterWithPotentialLeak<ProfileView>(analytic) {

    abstract fun initProfile()

    abstract fun initProfile(profileId: Long)

    abstract fun showStreakForStoredUser()
}
