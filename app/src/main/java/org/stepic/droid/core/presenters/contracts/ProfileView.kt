package org.stepic.droid.core.presenters.contracts

interface ProfileView {
    fun showNameImageShortBio(fullName: String, imageLink: String?, shortBio: String, isMyProfile: Boolean)
}
