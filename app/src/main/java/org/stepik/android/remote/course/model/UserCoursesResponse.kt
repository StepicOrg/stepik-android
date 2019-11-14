package org.stepik.android.remote.course.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.Meta
import org.stepik.android.model.UserCourse
import org.stepik.android.remote.base.model.MetaResponse

class UserCoursesResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("user-courses")
    val userCourse: List<UserCourse>
) : MetaResponse