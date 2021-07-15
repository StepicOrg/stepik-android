package org.stepik.android.remote.course_revenue.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.domain.course_revenue.model.CourseBenefitSummary
import org.stepik.android.model.Meta
import org.stepik.android.remote.base.model.MetaResponse

class CourseBenefitSummariesResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("course-benefit-summaries")
    val courseBenefitSummaries: List<CourseBenefitSummary>
) : MetaResponse