package org.stepic.droid.social

import android.support.annotation.DrawableRes
import org.stepic.droid.R


enum class SocialMedia(
    val link: String,
    @DrawableRes
    val drawable: Int
) {
    VK("https://vk.com/rustepik", R.drawable.ic_login_social_vk),
    FACEBOOK("https://www.facebook.com/rustepik/", R.drawable.ic_login_social_fb),
    INSTAGRAM("https://www.instagram.com/stepik.education/", R.drawable.ic_social_instagram)
}