package org.stepic.droid.core.presenters.contracts

interface ProfileView {

    fun showLoadingAll()

    fun showNameImageShortBio(fullName: String, imageLink: String?, shortBio: String, isMyProfile: Boolean, information : String)

    fun streaksIsLoaded(currentStreak: Int, maxStreak: Int)
}
