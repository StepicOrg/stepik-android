package org.stepik.android.model

import com.google.gson.annotations.SerializedName

class ViewAssignment(
    @SerializedName("assignment")
    val assignment: Long?,
    @SerializedName("step")
    val step: Long
)
