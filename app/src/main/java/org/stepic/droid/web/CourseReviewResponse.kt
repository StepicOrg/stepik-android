package org.stepic.droid.web

import com.google.gson.annotations.SerializedName
import org.stepic.droid.model.CourseReviewSummary
import org.stepik.android.model.Meta

class CourseReviewResponse(
        meta: Meta,
        @SerializedName("course-review-summaries")
        val courseReviewSummaries: List<CourseReviewSummary>
) : MetaResponseBase(meta)
