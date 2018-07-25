package org.stepic.droid.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Profile(
        val id: Long = 0,
        val bit_field: Long = 0,
        val level: Long = 0,
        val first_name: String? = null,
        val last_name: String? = null,
        val is_private: Boolean? = null,
        private val avatar: String? = null,
        val language: String? = null,
        val short_bio: String? = null,
        val details: String? = null,
        val notification_email_delay: String? = null,
        val level_title: String? = null,
        val subscribed_for_mail: Boolean = false,
        val is_staff: Boolean = false,
        val is_guest: Boolean = false,
        val can_add_lesson: Boolean = false,
        val can_add_course: Boolean = false,
        val can_add_group: Boolean = false,
        val subscribed_for_news_en: Boolean = false,
        val subscribed_for_news_ru: Boolean = false,
        @SerializedName("email_addresses")
        var emailAddresses: LongArray? = null
) : Serializable {
    fun getAvatarPath(): String? = avatar
}
