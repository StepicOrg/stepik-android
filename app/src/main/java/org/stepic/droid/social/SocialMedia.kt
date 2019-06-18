package org.stepic.droid.social

import android.support.annotation.DrawableRes
import org.stepic.droid.R


enum class SocialMedia(
    val link: String,
    @DrawableRes
    val drawable: Int
) {
    vk("https://vk.com/rustepik", R.drawable.ic_login_social_vk),
    facebook("https://www.facebook.com/rustepik/", R.drawable.ic_login_social_fb),
    instagram("https://www.instagram.com/stepik.education/", R.drawable.ic_social_instagram)
}