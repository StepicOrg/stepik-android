package org.stepik.android.view.auth.model

import androidx.annotation.DrawableRes
import org.stepic.droid.R
import org.stepik.android.domain.auth.model.SocialAuthType

enum class SocialNetwork(
    override val identifier: String,

    @DrawableRes
    val drawableRes: Int,

    override val isNeedUseAccessTokenInsteadOfCode: Boolean = false
) : SocialAuthType {
    GOOGLE("google", R.drawable.ic_login_social_google),
    VK("vk", R.drawable.ic_login_social_vk, isNeedUseAccessTokenInsteadOfCode = true),
    FACEBOOK("facebook", R.drawable.ic_login_social_fb, isNeedUseAccessTokenInsteadOfCode = true),
    TWITTER("twitter", R.drawable.ic_login_social_twitter),
    GITHUB("github", R.drawable.ic_login_social_github)
}