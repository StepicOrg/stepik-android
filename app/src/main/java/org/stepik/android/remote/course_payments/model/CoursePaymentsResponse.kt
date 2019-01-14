package org.stepik.android.remote.course_payments.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.Meta

class CoursePaymentsResponse(
    @SerializedName("meta")
    val meta: Meta,

    @SerializedName("course-payments")
    val coursePayments: List<*>
)