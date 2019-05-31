package org.stepik.android.remote.course.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.Enrollment

class EnrollmentRequest(
    @SerializedName("enrollment")
    val enrollment: Enrollment
)