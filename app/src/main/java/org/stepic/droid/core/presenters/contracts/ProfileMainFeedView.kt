package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.model.Profile

interface ProfileMainFeedView {
    fun showAnonymous()

    fun showProfile(profile: Profile)

    fun showLogoutLoading()

    fun onLogoutSuccess()
}