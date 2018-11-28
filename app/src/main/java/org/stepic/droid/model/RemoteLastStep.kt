package org.stepic.droid.model

import com.google.gson.annotations.SerializedName

class RemoteLastStep(
    /**
     * lastStepId != courseId
     */
    @SerializedName("id")
    val id: String,
    @SerializedName("unit")
    val unit: Long?,
    @SerializedName("step")
    val step: Long?
)