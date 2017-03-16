package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.model.UserViewModel

interface ProfileView {

    fun showLoadingAll()

    fun showNameImageShortBio(userViewModel: UserViewModel)

    fun streaksAreLoaded(currentStreak: Int, maxStreak: Int)

    fun onInternetFailed()

    fun onProfileNotFound()
}
