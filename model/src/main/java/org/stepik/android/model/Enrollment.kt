package org.stepik.android.model

import com.google.gson.annotations.SerializedName

class Enrollment(
    @SerializedName("course")
    val course: Long
)
