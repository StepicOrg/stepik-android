package org.stepik.android.domain.auth.model

interface SocialAuthType {
    val identifier: String
    val isNeedUseAccessTokenInsteadOfCode: Boolean
}