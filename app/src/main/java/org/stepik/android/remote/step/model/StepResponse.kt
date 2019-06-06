package org.stepik.android.remote.step.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.Meta
import org.stepik.android.model.Step
import org.stepik.android.remote.base.model.MetaResponse

class StepResponse(
    @SerializedName("meta")
    override val meta: Meta,

    @SerializedName("steps")
    val steps: List<Step>
) : MetaResponse