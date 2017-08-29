package org.stepic.droid.web

data class SocialAuthError(
        var error: String?,
        var email: String?,
        var provider: String?
)