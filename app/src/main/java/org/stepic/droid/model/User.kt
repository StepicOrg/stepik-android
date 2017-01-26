package org.stepic.droid.model

data class User(
        var id: Int = 0,
        var profile: Int = 0,
        var is_private: Boolean = false,
        var details: String? = null,
        var short_bio: String? = null,
        var first_name: String? = null,
        var last_name: String? = null,
        val avatar: String? = null,
        var level_title: String? = null,
        var level: Int = 0,
        var score_learn: Int = 0,
        var score_teach: Int = 0,
        var leaders: IntArray? = null
) {
//    fun getSvgAvatarPath(): String? {
//        if (BuildConfig.DEBUG) {
//            return "https://stepik.org/static/frontend/topbar_logo.svg" //fixme: remove debug
//        } else {
//            if (avatar != null && avatar.endsWith(".svg", ignoreCase = true)) {
//                return avatar
//            } else {
//                return null
//            }
//        }
//    }
}