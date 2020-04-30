package org.stepik.android.remote.user_courses.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.domain.user_courses.model.UserCourse

class UserCoursesRequest(
    @SerializedName("userCourse")
    val userCourse: UserCourse
)