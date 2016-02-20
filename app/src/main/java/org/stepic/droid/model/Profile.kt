package org.stepic.droid.model

import java.io.Serializable

data class Profile(
        val id: Long = 0,
        val bit_field: Long = 0,
        val level: Long = 0,
        val first_name: String? = null,
        val last_name: String? = null,
        val is_private: Boolean? = null,
        val avatar: String? = null,
        val language: String? = null,
        val short_bio: String? = null,
        val details: String? = null,
        val notification_email_delay: String? = null,
        val level_title: String? = null,
        val isSubscribed_for_mail: Boolean = false,
        val is_staff: Boolean = false,
        val is_guest: Boolean = false,
        val isCan_add_lesson: Boolean = false,
        val isCan_add_course: Boolean = false,
        val isCan_add_group: Boolean = false,
        val isSubscribed_for_news_en: Boolean = false,
        val isSubscribed_for_news_ru: Boolean = false
        ) : Serializable
