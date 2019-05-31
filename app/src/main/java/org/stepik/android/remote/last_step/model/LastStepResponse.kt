package org.stepik.android.remote.last_step.model

import com.google.gson.annotations.SerializedName
import org.stepic.droid.model.RemoteLastStep
import org.stepik.android.model.Meta
import org.stepik.android.remote.base.model.MetaResponse

class LastStepResponse(
    @SerializedName("meta")
    override val meta: Meta,
    @SerializedName("last-steps")
    val lastSteps: List<RemoteLastStep>
) : MetaResponse