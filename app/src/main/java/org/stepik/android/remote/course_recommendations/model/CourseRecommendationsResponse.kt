package org.stepik.android.remote.course_recommendations.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.domain.course_recommendations.model.CourseRecommendation
import org.stepik.android.model.Meta
import org.stepik.android.remote.base.model.MetaResponse

class CourseRecommendationsResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("course-recommendations")
    val courseRecommendations: List<CourseRecommendation>
) : MetaResponse