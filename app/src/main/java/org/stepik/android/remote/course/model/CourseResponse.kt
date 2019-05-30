package org.stepik.android.remote.course.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.Course
import org.stepik.android.model.Meta
import org.stepik.android.remote.base.model.MetaResponse

class CourseResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("courses")
    val courses: List<Course>
) : MetaResponse
