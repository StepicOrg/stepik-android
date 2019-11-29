package org.stepik.android.domain.profile.model

import org.stepik.android.model.user.User

data class ProfileData(
    val user: User,
    val isCurrentUser: Boolean
)