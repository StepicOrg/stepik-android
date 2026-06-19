package org.stepic.droid.social

import androidx.annotation.DrawableRes
import org.stepic.droid.R


enum class SocialMedia(
    val link: String,
    @DrawableRes
    val drawable: Int
) {
    VK("https://vk.com/rustepik", R.drawable.ic_login_social_vk)
}