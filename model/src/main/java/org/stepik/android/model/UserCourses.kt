package org.stepik.android.model

import com.google.gson.annotations.SerializedName

class UserCourses(
        val meta: Meta,
        @SerializedName("user-courses")
        val userCourse: List<UserCourse>
)