package org.stepic.droid.core.presenters.contracts

import org.stepik.android.model.user.Profile

interface ProfileMainFeedView {
    fun showAnonymous()

    fun showProfile(profile: Profile)

    fun showLogoutLoading()

    fun onLogoutSuccess()
}