package org.stepic.droid.model

import com.google.gson.annotations.SerializedName

data class User(
        var id: Long = 0,
        var profile: Int = 0,
        var is_private: Boolean = false,
        var details: String? = null,
        var short_bio: String? = null,
        var first_name: String? = null,
        var last_name: String? = null,
        @SerializedName("full_name")
        var fullName: String? = null,
        private val avatar: String? = null,
        var level_title: String? = null,
        var level: Int = 0,
        var score_learn: Int = 0,
        var score_teach: Int = 0,
        var leaders: IntArray? = null
        ) {
    fun getAvatarPath(): String? {
        return avatar;
    }
}