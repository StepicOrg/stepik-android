package org.stepik.android.remote.step_source.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.StepSource

class StepSourceRequest(
    @SerializedName("stepSource")
    val stepSource: StepSource
)