package org.stepik.android.remote.course.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.CourseReviewSummary
import org.stepik.android.model.Meta
import org.stepik.android.remote.base.model.MetaResponse

class CourseReviewSummaryResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("course-review-summaries")
    val courseReviewSummaries: List<CourseReviewSummary>
) : MetaResponse
