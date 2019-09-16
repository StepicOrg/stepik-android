package org.stepik.android.model.user

data class ProfileWrapper(
    val profile: Profile,
    val primaryEmailAdress: EmailAddress? = null
)