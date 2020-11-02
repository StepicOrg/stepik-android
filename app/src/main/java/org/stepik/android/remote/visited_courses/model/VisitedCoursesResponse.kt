package org.stepik.android.remote.visited_courses.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.domain.visited_courses.model.VisitedCourse
import org.stepik.android.model.Meta
import org.stepik.android.remote.base.model.MetaResponse

class VisitedCoursesResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("visited-courses")
    val visitedCourses: List<VisitedCourse>
) : MetaResponse