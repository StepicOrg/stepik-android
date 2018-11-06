package org.stepic.droid.model

class UserViewModel(
        val fullName: String,
        val shortBio: String,
        val information: String,
        val imageLink: String?,
        val isMyProfile: Boolean,
        val isPrivate: Boolean,
        val id: Long
)
