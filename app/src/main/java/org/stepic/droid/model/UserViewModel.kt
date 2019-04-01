package org.stepic.droid.model

import org.stepik.android.model.user.Profile

data class UserViewModel(
        val fullName: String,
        val shortBio: String,
        val information: String,
        val imageLink: String?,
        val isMyProfile: Boolean,
        val isPrivate: Boolean,
        val id: Long,
        val profile: Profile?
)
