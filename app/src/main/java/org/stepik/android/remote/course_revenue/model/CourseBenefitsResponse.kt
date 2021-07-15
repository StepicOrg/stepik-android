package org.stepik.android.remote.course_revenue.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.domain.course_revenue.model.CourseBenefit
import org.stepik.android.model.Meta
import org.stepik.android.remote.base.model.MetaResponse

class CourseBenefitsResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("course-benefits")
    val courseBenefits: List<CourseBenefit>
) : MetaResponse