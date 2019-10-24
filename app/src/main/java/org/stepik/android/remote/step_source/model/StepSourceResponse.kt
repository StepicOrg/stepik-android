package org.stepik.android.remote.step_source.model

import com.google.gson.annotations.SerializedName
import org.stepik.android.model.Meta
import org.stepik.android.model.StepSource
import org.stepik.android.remote.base.model.MetaResponse

class StepSourceResponse(
    @SerializedName("meta")
    override val meta: Meta,

    @SerializedName("step-sources")
    val stepSources: List<StepSource>
) : MetaResponse