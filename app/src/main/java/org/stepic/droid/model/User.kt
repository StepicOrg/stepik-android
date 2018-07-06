package org.stepic.droid.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class User(
        var id: Long = 0,
        var profile: Int = 0,
        @SerializedName("is_private") var isPrivate: Boolean = false,
        var details: String? = null,
        var short_bio: String? = null,
        var first_name: String? = null,
        var last_name: String? = null,
        @SerializedName("full_name") var fullName: String? = null,
        private val avatar: String? = null,
        var level_title: String? = null,
        var level: Int = 0,
        var score_learn: Int = 0,
        var score_teach: Int = 0,
        var leaders: IntArray? = null,

        @SerializedName("join_date") val joinDate: Date?
) {
    fun getAvatarPath(): String? = avatar
}