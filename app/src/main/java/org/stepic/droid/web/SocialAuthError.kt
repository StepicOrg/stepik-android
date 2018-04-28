package org.stepic.droid.web

data class SocialAuthError(
        val error: String?,
        val email: String?,
        val provider: String?
)